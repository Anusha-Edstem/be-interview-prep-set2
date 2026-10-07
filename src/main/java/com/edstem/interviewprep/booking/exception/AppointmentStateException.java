package com.edstem.interviewprep.booking.exception;

import com.edstem.interviewprep.booking.entity.AppointmentStatus;
import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class AppointmentStateException extends ApiException {

  public AppointmentStateException(UUID id, AppointmentStatus status, String expected) {
    super(
        HttpStatus.CONFLICT,
        "APPOINTMENT_STATE_INVALID",
        "Appointment " + id + " is " + status + ", and this action needs it to be " + expected);
  }
}
