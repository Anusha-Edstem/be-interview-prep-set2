package com.edstem.interviewprep.book.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class BookNotBorrowedException extends ApiException {

  public BookNotBorrowedException(UUID id) {
    super(
        HttpStatus.CONFLICT,
        "BOOK_NOT_BORROWED",
        "Book " + id + " is not currently borrowed, so it cannot be returned");
  }
}
