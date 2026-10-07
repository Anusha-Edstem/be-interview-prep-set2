package com.edstem.interviewprep.expense.mapper;

import com.edstem.interviewprep.expense.dto.response.ExpenseResponse;
import com.edstem.interviewprep.expense.entity.Expense;

public final class ExpenseMapper {

  private ExpenseMapper() {}

  public static ExpenseResponse toResponse(Expense expense) {
    return new ExpenseResponse(
        expense.getId(),
        expense.getAmount(),
        expense.getCategory(),
        expense.getSpentOn(),
        expense.getNote());
  }
}
