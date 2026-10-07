package com.edstem.interviewprep.booking.dto.response;

import com.edstem.interviewprep.booking.entity.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
    UUID id,
    UUID doctorId,
    String patientId,
    Instant startsAt,
    AppointmentStatus status,
    Instant heldUntil,
    Instant confirmedAt) {}
