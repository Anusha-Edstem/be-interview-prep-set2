package com.edstem.interviewprep.expense.dto.request;

import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
    @NotNull
        @DecimalMin(value = "0.00", inclusive = false, message = "must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "must have at most 2 decimal places")
        BigDecimal amount,
    @NotNull ExpenseCategory category,
    @NotNull LocalDate spentOn,
    @Size(max = 500) String note) {}
