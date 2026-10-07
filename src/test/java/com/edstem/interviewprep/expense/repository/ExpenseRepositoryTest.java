package com.edstem.interviewprep.expense.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edstem.interviewprep.expense.entity.Expense;
import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ExpenseRepositoryTest {

  @Autowired private ExpenseRepository expenseRepository;

  @Test
  void theCategoryTotalIncludesTheFirstAndLastDayAndExcludesNeighbouringMonths() {
    save("10.00", ExpenseCategory.FOOD, LocalDate.of(2026, 3, 1));
    save("20.00", ExpenseCategory.FOOD, LocalDate.of(2026, 3, 31));
    save("99.00", ExpenseCategory.FOOD, LocalDate.of(2026, 2, 28));
    save("77.00", ExpenseCategory.FOOD, LocalDate.of(2026, 4, 1));
    expenseRepository.flush();

    Map<ExpenseCategory, BigDecimal> totals =
        totalsFor(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

    assertThat(totals.get(ExpenseCategory.FOOD)).isEqualByComparingTo("30.00");
  }

  @Test
  void summingFractionalAmountsLosesNoPrecision() {
    save("0.10", ExpenseCategory.FOOD, LocalDate.of(2026, 3, 10));
    save("0.20", ExpenseCategory.FOOD, LocalDate.of(2026, 3, 11));
    expenseRepository.flush();

    Map<ExpenseCategory, BigDecimal> totals =
        totalsFor(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

    assertThat(totals.get(ExpenseCategory.FOOD)).isEqualByComparingTo("0.30");
  }

  @Test
  void eachCategoryIsTotalledSeparately() {
    save("10.00", ExpenseCategory.FOOD, LocalDate.of(2026, 3, 10));
    save("5.50", ExpenseCategory.TRAVEL, LocalDate.of(2026, 3, 11));
    save("4.50", ExpenseCategory.TRAVEL, LocalDate.of(2026, 3, 12));
    expenseRepository.flush();

    Map<ExpenseCategory, BigDecimal> totals =
        totalsFor(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

    assertThat(totals.get(ExpenseCategory.FOOD)).isEqualByComparingTo("10.00");
    assertThat(totals.get(ExpenseCategory.TRAVEL)).isEqualByComparingTo("10.00");
    assertThat(totals).doesNotContainKey(ExpenseCategory.BILLS);
  }

  @Test
  void aRangeWithNoExpensesTotalsNothing() {
    save("10.00", ExpenseCategory.FOOD, LocalDate.of(2026, 3, 10));
    expenseRepository.flush();

    assertThat(totalsFor(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30))).isEmpty();
  }

  private Map<ExpenseCategory, BigDecimal> totalsFor(LocalDate start, LocalDate end) {
    List<CategoryTotal> totals = expenseRepository.totalPerCategoryBetween(start, end);
    return totals.stream()
        .collect(Collectors.toMap(CategoryTotal::getCategory, Function.identity()))
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTotal()));
  }

  private void save(String amount, ExpenseCategory category, LocalDate spentOn) {
    expenseRepository.save(Expense.create(new BigDecimal(amount), category, spentOn, null));
  }
}
