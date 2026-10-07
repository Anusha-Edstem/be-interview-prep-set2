package com.edstem.interviewprep.booking.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AvailableSlotsResponse(UUID doctorId, LocalDate date, List<Instant> availableSlots) {}
