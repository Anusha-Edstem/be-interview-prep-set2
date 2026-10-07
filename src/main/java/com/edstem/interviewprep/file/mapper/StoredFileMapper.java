package com.edstem.interviewprep.file.mapper;

import com.edstem.interviewprep.file.dto.response.StoredFileResponse;
import com.edstem.interviewprep.file.entity.StoredFile;

public final class StoredFileMapper {

  private StoredFileMapper() {}

  public static StoredFileResponse toResponse(StoredFile file) {
    return new StoredFileResponse(
        file.getId(),
        file.getOriginalName(),
        file.getFileType().getContentType(),
        file.getSizeBytes(),
        file.getUploadedAt());
  }
}
