package com.edstem.interviewprep.file.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UnsupportedFileTypeException extends ApiException {

  public UnsupportedFileTypeException() {
    super(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "UNSUPPORTED_FILE_TYPE",
        "Only JPEG, PNG and PDF content is accepted, judged by the file content itself");
  }
}
