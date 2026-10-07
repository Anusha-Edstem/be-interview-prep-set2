package com.edstem.interviewprep.file.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmptyFileException extends ApiException {

  public EmptyFileException() {
    super(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "The uploaded file carries no content");
  }
}
