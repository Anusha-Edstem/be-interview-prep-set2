package com.edstem.interviewprep.book.dto.request;

import com.edstem.interviewprep.book.validation.NotFutureYear;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBookRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 255) String author,
    @NotBlank @Size(max = 32) String isbn,
    @NotNull @NotFutureYear Integer publishedYear) {}
