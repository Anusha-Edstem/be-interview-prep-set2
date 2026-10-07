package com.edstem.interviewprep.book.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class IsbnAlreadyRegisteredException extends ApiException {

  public IsbnAlreadyRegisteredException(String isbn) {
    super(
        HttpStatus.CONFLICT,
        "ISBN_ALREADY_REGISTERED",
        "Another book is already registered with ISBN " + isbn);
  }
}
