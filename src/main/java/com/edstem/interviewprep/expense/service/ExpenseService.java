package com.edstem.interviewprep.expense.service;

import com.edstem.interviewprep.common.dto.response.PageResponse;
import com.edstem.interviewprep.expense.dto.request.CreateExpenseRequest;
import com.edstem.interviewprep.expense.dto.request.UpdateExpenseRequest;
import com.edstem.interviewprep.expense.dto.response.ExpenseResponse;
import com.edstem.interviewprep.expense.dto.response.MonthlySummaryResponse;
import com.edstem.interviewprep.expense.entity.Expense;
import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import com.edstem.interviewprep.expense.exception.ExpenseNotFoundException;
import com.edstem.interviewprep.expense.exception.InvalidDateRangeException;
import com.edstem.interviewprep.expense.mapper.ExpenseMapper;
import com.edstem.interviewprep.expense.repository.CategoryTotal;
import com.edstem.interviewprep.expense.repository.ExpenseRepository;
import com.edstem.interviewprep.expense.repository.ExpenseSpecifications;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

  private static final int AMOUNT_SCALE = 2;

  private final ExpenseRepository expenseRepository;

  public ExpenseService(ExpenseRepository expenseRepository) {
    this.expenseRepository = expenseRepository;
  }

  @Transactional
  public ExpenseResponse createExpense(CreateExpenseRequest request) {
    Expense expense =
        Expense.create(
            scaled(request.amount()), request.category(), request.spentOn(), request.note());
    return ExpenseMapper.toResponse(expenseRepository.save(expense));
  }

  @Transactional(readOnly = true)
  public PageResponse<ExpenseResponse> listExpenses(
      LocalDate from, LocalDate to, ExpenseCategory category, Pageable pageable) {
    requireOrderedRange(from, to);
    Specification<Expense> filter =
        Specification.allOf(
            ExpenseSpecifications.spentOnOrAfter(from),
            ExpenseSpecifications.spentOnOrBefore(to),
            ExpenseSpecifications.inCategory(category));
    Page<Expense> expenses = expenseRepository.findAll(filter, pageable);
    return PageResponse.from(expenses.map(ExpenseMapper::toResponse));
  }

  @Transactional(readOnly = true)
  public ExpenseResponse getExpense(UUID id) {
    return ExpenseMapper.toResponse(findExpenseOrThrow(id));
  }

  @Transactional
  public ExpenseResponse updateExpense(UUID id, UpdateExpenseRequest request) {
    Expense expense = findExpenseOrThrow(id);
    expense.update(scaled(request.amount()), request.category(), request.spentOn(), request.note());
    return ExpenseMapper.toResponse(expense);
  }

  @Transactional
  public void deleteExpense(UUID id) {
    expenseRepository.delete(findExpenseOrThrow(id));
  }

  @Transactional(readOnly = true)
  public MonthlySummaryResponse summarise(YearMonth month) {
    LocalDate from = month.atDay(1);
    LocalDate to = month.atEndOfMonth();
    List<CategoryTotal> totals = expenseRepository.totalPerCategoryBetween(from, to);

    Map<ExpenseCategory, BigDecimal> perCategory = new EnumMap<>(ExpenseCategory.class);
    for (ExpenseCategory category : ExpenseCategory.values()) {
      perCategory.put(category, zero());
    }
    for (CategoryTotal total : totals) {
      perCategory.put(total.getCategory(), scaled(total.getTotal()));
    }

    BigDecimal overall =
        perCategory.values().stream()
            .reduce(zero(), BigDecimal::add)
            .setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);

    return new MonthlySummaryResponse(month.toString(), from, to, perCategory, overall);
  }

  private void requireOrderedRange(LocalDate from, LocalDate to) {
    if (from != null && to != null && from.isAfter(to)) {
      throw new InvalidDateRangeException(from, to);
    }
  }

  private Expense findExpenseOrThrow(UUID id) {
    return expenseRepository.findById(id).orElseThrow(() -> new ExpenseNotFoundException(id));
  }

  private BigDecimal scaled(BigDecimal amount) {
    return amount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
  }

  private BigDecimal zero() {
    return BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
  }
}
