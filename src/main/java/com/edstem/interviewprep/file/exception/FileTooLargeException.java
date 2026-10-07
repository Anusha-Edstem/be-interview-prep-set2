package com.edstem.interviewprep.file.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class FileTooLargeException extends ApiException {

  public FileTooLargeException(long sizeBytes, long maxBytes) {
    super(
        HttpStatus.PAYLOAD_TOO_LARGE,
        "FILE_TOO_LARGE",
        "The file is " + sizeBytes + " bytes, which is over the limit of " + maxBytes + " bytes");
  }
}
