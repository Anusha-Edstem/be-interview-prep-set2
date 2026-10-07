package com.edstem.interviewprep.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BorrowBookRequest(@NotBlank @Size(max = 100) String memberId) {}
