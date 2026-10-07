package com.edstem.interviewprep.file.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class StoredFileNotFoundException extends ApiException {

  public StoredFileNotFoundException(UUID id) {
    super(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "No file exists with id " + id);
  }
}
