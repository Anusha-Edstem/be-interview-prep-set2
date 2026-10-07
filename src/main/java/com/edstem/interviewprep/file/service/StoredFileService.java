package com.edstem.interviewprep.file.service;

import com.edstem.interviewprep.common.dto.response.PageResponse;
import com.edstem.interviewprep.file.config.FileStorageProperties;
import com.edstem.interviewprep.file.dto.response.StoredFileResponse;
import com.edstem.interviewprep.file.entity.FileType;
import com.edstem.interviewprep.file.entity.StoredFile;
import com.edstem.interviewprep.file.exception.EmptyFileException;
import com.edstem.interviewprep.file.exception.FileStorageException;
import com.edstem.interviewprep.file.exception.FileTooLargeException;
import com.edstem.interviewprep.file.exception.StoredFileNotFoundException;
import com.edstem.interviewprep.file.exception.UnsupportedFileTypeException;
import com.edstem.interviewprep.file.mapper.StoredFileMapper;
import com.edstem.interviewprep.file.repository.StoredFileRepository;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StoredFileService {

  private final StoredFileRepository storedFileRepository;
  private final FileStorage fileStorage;
  private final FileStorageProperties properties;
  private final Clock clock;

  public StoredFileService(
      StoredFileRepository storedFileRepository,
      FileStorage fileStorage,
      FileStorageProperties properties,
      Clock clock) {
    this.storedFileRepository = storedFileRepository;
    this.fileStorage = fileStorage;
    this.properties = properties;
    this.clock = clock;
  }

  @Transactional
  public StoredFileResponse upload(MultipartFile submitted) {
    if (submitted == null || submitted.isEmpty()) {
      throw new EmptyFileException();
    }
    long maxBytes = properties.maxFileSize().toBytes();
    if (submitted.getSize() > maxBytes) {
      throw new FileTooLargeException(submitted.getSize(), maxBytes);
    }

    try (InputStream content = new BufferedInputStream(submitted.getInputStream())) {
      FileType fileType = detectType(content);
      String storageKey = UUID.randomUUID() + fileType.getExtension();
      fileStorage.store(storageKey, content);
      StoredFile record =
          StoredFile.create(
              UploadedFileName.sanitise(submitted.getOriginalFilename()),
              fileType,
              submitted.getSize(),
              storageKey,
              Instant.now(clock));
      return StoredFileMapper.toResponse(storedFileRepository.save(record));
    } catch (IOException exception) {
      throw new FileStorageException("The uploaded file could not be read");
    }
  }

  @Transactional(readOnly = true)
  public PageResponse<StoredFileResponse> listFiles(Pageable pageable) {
    Page<StoredFile> files = storedFileRepository.findAll(pageable);
    return PageResponse.from(files.map(StoredFileMapper::toResponse));
  }

  @Transactional(readOnly = true)
  public DownloadableFile download(UUID id) {
    StoredFile record = findFileOrThrow(id);
    Resource content = fileStorage.load(record.getStorageKey());
    return new DownloadableFile(
        content, record.getOriginalName(), record.getFileType().getContentType());
  }

  @Transactional
  public void deleteFile(UUID id) {
    StoredFile record = findFileOrThrow(id);
    storedFileRepository.delete(record);
    fileStorage.delete(record.getStorageKey());
  }

  private FileType detectType(InputStream content) throws IOException {
    content.mark(FileType.SIGNATURE_LENGTH + 1);
    byte[] header = content.readNBytes(FileType.SIGNATURE_LENGTH);
    content.reset();
    return FileType.detect(header).orElseThrow(UnsupportedFileTypeException::new);
  }

  private StoredFile findFileOrThrow(UUID id) {
    return storedFileRepository.findById(id).orElseThrow(() -> new StoredFileNotFoundException(id));
  }
}
