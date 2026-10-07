package com.edstem.interviewprep.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AppointmentNotifier {

  private static final Logger log = LoggerFactory.getLogger(AppointmentNotifier.class);

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onAppointmentConfirmed(AppointmentConfirmedEvent event) {
    log.info(
        "Appointment {} confirmed for patient {} with doctor {} starting at {}",
        event.appointmentId(),
        event.patientId(),
        event.doctorId(),
        event.startsAt());
  }
}
