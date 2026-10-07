package com.edstem.interviewprep.book.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class BookNotFoundException extends ApiException {

  public BookNotFoundException(UUID id) {
    super(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND", "No book exists with id " + id);
  }
}
