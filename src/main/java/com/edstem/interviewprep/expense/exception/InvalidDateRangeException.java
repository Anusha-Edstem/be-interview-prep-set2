package com.edstem.interviewprep.expense.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;

public class InvalidDateRangeException extends ApiException {

  public InvalidDateRangeException(LocalDate from, LocalDate to) {
    super(
        HttpStatus.BAD_REQUEST,
        "INVALID_DATE_RANGE",
        "The range start " + from + " is after the range end " + to);
  }
}
