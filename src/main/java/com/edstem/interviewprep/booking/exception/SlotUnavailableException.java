package com.edstem.interviewprep.booking.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.time.Instant;
import org.springframework.http.HttpStatus;

public class SlotUnavailableException extends ApiException {

  public SlotUnavailableException(Instant startsAt) {
    super(
        HttpStatus.CONFLICT,
        "SLOT_UNAVAILABLE",
        "The slot starting at " + startsAt + " is already held or booked");
  }
}
