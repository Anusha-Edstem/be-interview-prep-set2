package com.edstem.interviewprep.book.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class BorrowedBookDeletionException extends ApiException {

  public BorrowedBookDeletionException(UUID id) {
    super(
        HttpStatus.CONFLICT,
        "BOOK_CURRENTLY_BORROWED",
        "Book " + id + " is currently borrowed and cannot be deleted until it is returned");
  }
}
