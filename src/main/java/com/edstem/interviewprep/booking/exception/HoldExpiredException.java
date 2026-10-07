package com.edstem.interviewprep.booking.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class HoldExpiredException extends ApiException {

  public HoldExpiredException(UUID id) {
    super(
        HttpStatus.CONFLICT,
        "HOLD_EXPIRED",
        "The hold on appointment " + id + " expired before it was confirmed");
  }
}
