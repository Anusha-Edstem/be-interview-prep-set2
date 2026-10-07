package com.edstem.interviewprep.book.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class BookAlreadyBorrowedException extends ApiException {

  public BookAlreadyBorrowedException(UUID id) {
    super(
        HttpStatus.CONFLICT,
        "BOOK_ALREADY_BORROWED",
        "Book " + id + " is already borrowed and cannot be borrowed again until it is returned");
  }
}
