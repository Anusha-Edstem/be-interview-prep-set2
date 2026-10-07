package com.edstem.interviewprep.expense.exception;

import com.edstem.interviewprep.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ExpenseNotFoundException extends ApiException {

  public ExpenseNotFoundException(UUID id) {
    super(HttpStatus.NOT_FOUND, "EXPENSE_NOT_FOUND", "No expense exists with id " + id);
  }
}
