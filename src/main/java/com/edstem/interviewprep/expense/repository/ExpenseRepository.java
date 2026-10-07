package com.edstem.interviewprep.expense.repository;

import com.edstem.interviewprep.expense.entity.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository
    extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

  @Query(
      """
      select e.category as category, sum(e.amount) as total
      from Expense e
      where e.spentOn >= :start and e.spentOn <= :end
      group by e.category
      """)
  List<CategoryTotal> totalPerCategoryBetween(
      @Param("start") LocalDate start, @Param("end") LocalDate end);
}
