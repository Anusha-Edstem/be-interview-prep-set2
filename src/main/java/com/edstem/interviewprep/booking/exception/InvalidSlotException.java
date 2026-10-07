package com.edstem.interviewprep.booking.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.time.Instant;
import org.springframework.http.HttpStatus;

public class InvalidSlotException extends ApiException {

  public InvalidSlotException(Instant startsAt) {
    super(
        HttpStatus.BAD_REQUEST,
        "INVALID_SLOT",
        startsAt + " is not the start of a bookable slot for this doctor");
  }
}
