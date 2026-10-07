package com.edstem.interviewprep.file.controller;

import com.edstem.interviewprep.common.dto.response.PageResponse;
import com.edstem.interviewprep.file.dto.response.StoredFileResponse;
import com.edstem.interviewprep.file.service.DownloadableFile;
import com.edstem.interviewprep.file.service.StoredFileService;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class StoredFileController {

  private final StoredFileService storedFileService;

  public StoredFileController(StoredFileService storedFileService) {
    this.storedFileService = storedFileService;
  }

  @PostMapping
  public ResponseEntity<StoredFileResponse> upload(@RequestParam("file") MultipartFile file) {
    StoredFileResponse stored = storedFileService.upload(file);
    return ResponseEntity.created(URI.create("/api/v1/files/" + stored.id())).body(stored);
  }

  @GetMapping
  public ResponseEntity<PageResponse<StoredFileResponse>> listFiles(
      @PageableDefault(size = 20, sort = "uploadedAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(storedFileService.listFiles(pageable));
  }

  @GetMapping("/{id}/content")
  public ResponseEntity<Resource> download(@PathVariable UUID id) {
    DownloadableFile file = storedFileService.download(id);
    ContentDisposition disposition =
        ContentDisposition.attachment()
            .filename(file.originalName(), StandardCharsets.UTF_8)
            .build();
    return ResponseEntity.ok()
        .header("Content-Disposition", disposition.toString())
        .contentType(MediaType.parseMediaType(file.contentType()))
        .body(file.content());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
    storedFileService.deleteFile(id);
    return ResponseEntity.noContent().build();
  }
}
