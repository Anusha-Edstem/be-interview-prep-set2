package com.edstem.interviewprep.expense.repository;

import com.edstem.interviewprep.expense.entity.Expense;
import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecifications {

  private ExpenseSpecifications() {}

  public static Specification<Expense> spentOnOrAfter(LocalDate from) {
    return from == null
        ? null
        : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("spentOn"), from);
  }

  public static Specification<Expense> spentOnOrBefore(LocalDate to) {
    return to == null
        ? null
        : (root, query, builder) -> builder.lessThanOrEqualTo(root.get("spentOn"), to);
  }

  public static Specification<Expense> inCategory(ExpenseCategory category) {
    return category == null
        ? null
        : (root, query, builder) -> builder.equal(root.get("category"), category);
  }
}
