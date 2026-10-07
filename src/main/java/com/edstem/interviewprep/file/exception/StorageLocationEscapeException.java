package com.edstem.interviewprep.file.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class StorageLocationEscapeException extends ApiException {

  public StorageLocationEscapeException() {
    super(
        HttpStatus.BAD_REQUEST,
        "INVALID_STORAGE_KEY",
        "The requested file resolves outside the storage location");
  }
}
