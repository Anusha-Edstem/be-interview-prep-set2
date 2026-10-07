package com.edstem.interviewprep.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record HoldSlotRequest(
    @NotNull UUID doctorId,
    @NotBlank @Size(max = 100) String patientId,
    @NotNull Instant startsAt) {}
