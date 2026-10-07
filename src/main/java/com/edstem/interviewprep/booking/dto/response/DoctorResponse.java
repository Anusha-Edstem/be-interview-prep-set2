package com.edstem.interviewprep.booking.dto.response;

import java.util.UUID;

public record DoctorResponse(UUID id, String name, String speciality) {}
