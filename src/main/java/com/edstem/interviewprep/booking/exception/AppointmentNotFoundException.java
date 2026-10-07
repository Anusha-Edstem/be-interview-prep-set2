package com.edstem.interviewprep.booking.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class AppointmentNotFoundException extends ApiException {

  public AppointmentNotFoundException(UUID id) {
    super(HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND", "No appointment exists with id " + id);
  }
}
