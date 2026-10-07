package com.edstem.interviewprep.booking.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class DoctorNotFoundException extends ApiException {

  public DoctorNotFoundException(UUID id) {
    super(HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND", "No doctor exists with id " + id);
  }
}
