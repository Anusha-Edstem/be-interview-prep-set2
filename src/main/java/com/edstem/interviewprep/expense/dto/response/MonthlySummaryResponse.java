package com.edstem.interviewprep.expense.dto.response;

import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record MonthlySummaryResponse(
    String month,
    LocalDate from,
    LocalDate to,
    Map<ExpenseCategory, BigDecimal> totalPerCategory,
    BigDecimal total) {}
