package com.edstem.interviewprep.booking.service;

import java.time.Instant;
import java.util.UUID;

public record AppointmentConfirmedEvent(
    UUID appointmentId, UUID doctorId, String patientId, Instant startsAt) {}
