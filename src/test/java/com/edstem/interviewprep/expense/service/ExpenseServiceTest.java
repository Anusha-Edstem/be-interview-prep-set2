package com.edstem.interviewprep.expense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.edstem.interviewprep.expense.dto.request.CreateExpenseRequest;
import com.edstem.interviewprep.expense.dto.response.ExpenseResponse;
import com.edstem.interviewprep.expense.dto.response.MonthlySummaryResponse;
import com.edstem.interviewprep.expense.entity.Expense;
import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import com.edstem.interviewprep.expense.exception.ExpenseNotFoundException;
import com.edstem.interviewprep.expense.exception.InvalidDateRangeException;
import com.edstem.interviewprep.expense.repository.CategoryTotal;
import com.edstem.interviewprep.expense.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

  @Mock private ExpenseRepository expenseRepository;

  @InjectMocks private ExpenseService expenseService;

  @Test
  void anAmountIsStoredWithTwoDecimalPlaces() {
    when(expenseRepository.save(any(Expense.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ExpenseResponse created =
        expenseService.createExpense(
            new CreateExpenseRequest(
                new BigDecimal("12.5"), ExpenseCategory.FOOD, LocalDate.of(2026, 3, 10), "lunch"));

    assertThat(created.amount()).isEqualByComparingTo("12.50");
    assertThat(created.amount().scale()).isEqualTo(2);
  }

  @Test
  void theMonthlySummaryAddsFractionalAmountsExactly() {
    when(expenseRepository.totalPerCategoryBetween(
            LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
        .thenReturn(
            List.of(
                categoryTotal(ExpenseCategory.FOOD, new BigDecimal("0.10")),
                categoryTotal(ExpenseCategory.TRAVEL, new BigDecimal("0.20"))));

    MonthlySummaryResponse summary = expenseService.summarise(YearMonth.of(2026, 3));

    assertThat(summary.total()).isEqualByComparingTo("0.30");
    assertThat(summary.total()).isEqualTo(new BigDecimal("0.30"));
  }

  @Test
  void theMonthlySummaryReportsEveryCategoryEvenWhenNothingWasSpent() {
    when(expenseRepository.totalPerCategoryBetween(
            LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
        .thenReturn(List.of(categoryTotal(ExpenseCategory.BILLS, new BigDecimal("45.00"))));

    MonthlySummaryResponse summary = expenseService.summarise(YearMonth.of(2026, 3));

    assertThat(summary.totalPerCategory())
        .containsOnlyKeys(ExpenseCategory.values())
        .containsEntry(ExpenseCategory.BILLS, new BigDecimal("45.00"))
        .containsEntry(ExpenseCategory.FOOD, new BigDecimal("0.00"));
    assertThat(summary.total()).isEqualByComparingTo("45.00");
  }

  @Test
  void theMonthlySummarySpansTheFirstAndLastDayOfTheMonth() {
    when(expenseRepository.totalPerCategoryBetween(
            LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
        .thenReturn(List.of());

    MonthlySummaryResponse summary = expenseService.summarise(YearMonth.of(2026, 2));

    assertThat(summary.from()).isEqualTo(LocalDate.of(2026, 2, 1));
    assertThat(summary.to()).isEqualTo(LocalDate.of(2026, 2, 28));
    assertThat(summary.month()).isEqualTo("2026-02");
  }

  @Test
  void theMonthlySummaryCoversTheExtraDayOfALeapFebruary() {
    when(expenseRepository.totalPerCategoryBetween(
            LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29)))
        .thenReturn(List.of());

    MonthlySummaryResponse summary = expenseService.summarise(YearMonth.of(2028, 2));

    assertThat(summary.to()).isEqualTo(LocalDate.of(2028, 2, 29));
  }

  @Test
  void listingWithAStartAfterTheEndIsRejected() {
    assertThrows(
        InvalidDateRangeException.class,
        () ->
            expenseService.listExpenses(
                LocalDate.of(2026, 3, 31), LocalDate.of(2026, 3, 1), null, PageRequest.of(0, 20)));
  }

  @Test
  void updatingAnExpenseThatDoesNotExistFails() {
    UUID id = UUID.randomUUID();
    when(expenseRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ExpenseNotFoundException.class, () -> expenseService.getExpense(id));
  }

  private CategoryTotal categoryTotal(ExpenseCategory category, BigDecimal total) {
    return new StubCategoryTotal(category, total);
  }

  private record StubCategoryTotal(ExpenseCategory category, BigDecimal total)
      implements CategoryTotal {

    @Override
    public ExpenseCategory getCategory() {
      return category;
    }

    @Override
    public BigDecimal getTotal() {
      return total;
    }
  }
}
