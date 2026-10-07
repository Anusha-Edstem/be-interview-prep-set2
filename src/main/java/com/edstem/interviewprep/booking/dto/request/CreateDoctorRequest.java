package com.edstem.interviewprep.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDoctorRequest(
    @NotBlank @Size(max = 150) String name, @NotBlank @Size(max = 100) String speciality) {}
