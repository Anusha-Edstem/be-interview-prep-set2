package com.edstem.interviewprep.expense.dto.response;

import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
    UUID id, BigDecimal amount, ExpenseCategory category, LocalDate spentOn, String note) {}
