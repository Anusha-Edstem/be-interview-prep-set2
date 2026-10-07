package com.edstem.interviewprep.ratelimit.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ApiKeyRequiredException extends ApiException {

  public ApiKeyRequiredException(String headerName) {
    super(
        HttpStatus.BAD_REQUEST,
        "API_KEY_REQUIRED",
        "The " + headerName + " header identifies the calling client and is required");
  }
}
