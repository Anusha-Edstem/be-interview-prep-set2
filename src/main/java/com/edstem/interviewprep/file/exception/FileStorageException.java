package com.edstem.interviewprep.file.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class FileStorageException extends ApiException {

  public FileStorageException(String message) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_STORAGE_FAILED", message);
  }
}
