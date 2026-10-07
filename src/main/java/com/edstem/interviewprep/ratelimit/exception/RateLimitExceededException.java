package com.edstem.interviewprep.ratelimit.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends ApiException {

  public RateLimitExceededException(int limit, long retryAfterSeconds) {
    super(
        HttpStatus.TOO_MANY_REQUESTS,
        "RATE_LIMIT_EXCEEDED",
        "This client has used its "
            + limit
            + " requests for the current window; retry in "
            + retryAfterSeconds
            + " seconds");
  }
}
