package com.edstem.interviewprep.book.dto.response;

import com.edstem.interviewprep.book.entity.BookStatus;
import java.time.Instant;
import java.util.UUID;

public record BookResponse(
    UUID id,
    String title,
    String author,
    String isbn,
    int publishedYear,
    BookStatus status,
    String borrowedBy,
    Instant borrowedAt) {}
