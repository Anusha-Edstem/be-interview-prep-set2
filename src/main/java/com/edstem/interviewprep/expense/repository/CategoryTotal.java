package com.edstem.interviewprep.expense.repository;

import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import java.math.BigDecimal;

public interface CategoryTotal {

  ExpenseCategory getCategory();

  BigDecimal getTotal();
}
