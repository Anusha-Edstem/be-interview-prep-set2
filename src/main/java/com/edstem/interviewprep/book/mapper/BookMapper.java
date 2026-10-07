package com.edstem.interviewprep.book.mapper;

import com.edstem.interviewprep.book.dto.response.BookResponse;
import com.edstem.interviewprep.book.entity.Book;

public final class BookMapper {

  private BookMapper() {}

  public static BookResponse toResponse(Book book) {
    return new BookResponse(
        book.getId(),
        book.getTitle(),
        book.getAuthor(),
        book.getIsbn(),
        book.getPublishedYear(),
        book.getStatus(),
        book.getBorrowedBy(),
        book.getBorrowedAt());
  }
}
