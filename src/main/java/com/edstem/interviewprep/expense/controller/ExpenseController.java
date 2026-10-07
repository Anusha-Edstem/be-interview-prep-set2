package com.edstem.interviewprep.expense.controller;

import com.edstem.interviewprep.common.dto.response.PageResponse;
import com.edstem.interviewprep.expense.dto.request.CreateExpenseRequest;
import com.edstem.interviewprep.expense.dto.request.UpdateExpenseRequest;
import com.edstem.interviewprep.expense.dto.response.ExpenseResponse;
import com.edstem.interviewprep.expense.dto.response.MonthlySummaryResponse;
import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import com.edstem.interviewprep.expense.service.ExpenseService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

  private final ExpenseService expenseService;

  public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  @PostMapping
  public ResponseEntity<ExpenseResponse> createExpense(
      @Valid @RequestBody CreateExpenseRequest request) {
    ExpenseResponse created = expenseService.createExpense(request);
    return ResponseEntity.created(URI.create("/api/v1/expenses/" + created.id())).body(created);
  }

  @GetMapping
  public ResponseEntity<PageResponse<ExpenseResponse>> listExpenses(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) ExpenseCategory category,
      @PageableDefault(size = 20, sort = "spentOn", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(expenseService.listExpenses(from, to, category, pageable));
  }

  @GetMapping("/summary")
  public ResponseEntity<MonthlySummaryResponse> summarise(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
    return ResponseEntity.ok(expenseService.summarise(month));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ExpenseResponse> getExpense(@PathVariable UUID id) {
    return ResponseEntity.ok(expenseService.getExpense(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ExpenseResponse> updateExpense(
      @PathVariable UUID id, @Valid @RequestBody UpdateExpenseRequest request) {
    return ResponseEntity.ok(expenseService.updateExpense(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
    expenseService.deleteExpense(id);
    return ResponseEntity.noContent().build();
  }
}
