package com.edstem.interviewprep.expense.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.interviewprep.common.dto.response.PageResponse;
import com.edstem.interviewprep.expense.dto.response.ExpenseResponse;
import com.edstem.interviewprep.expense.dto.response.MonthlySummaryResponse;
import com.edstem.interviewprep.expense.entity.ExpenseCategory;
import com.edstem.interviewprep.expense.exception.ExpenseNotFoundException;
import com.edstem.interviewprep.expense.service.ExpenseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ExpenseService expenseService;

  @Test
  void recordingAnExpenseReturnsCreated() throws Exception {
    UUID id = UUID.randomUUID();
    when(expenseService.createExpense(any()))
        .thenReturn(
            new ExpenseResponse(
                id,
                new BigDecimal("12.50"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 3, 1),
                "lunch"));

    mockMvc
        .perform(
            post("/api/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"amount":12.50,"category":"FOOD","spentOn":"2026-03-01","note":"lunch"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.amount").value(12.50));
  }

  @Test
  void anUnknownCategoryIsRejectedWithTheAcceptedValues() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"amount":12.50,"category":"GROCERIES","spentOn":"2026-03-01"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("category"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message")
                .value("must be one of: FOOD, TRAVEL, BILLS, OTHER"));
  }

  @Test
  void theSummaryEndpointAcceptsAYearMonth() throws Exception {
    when(expenseService.summarise(YearMonth.of(2026, 3)))
        .thenReturn(
            new MonthlySummaryResponse(
                "2026-03",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                Map.of(ExpenseCategory.FOOD, new BigDecimal("0.30")),
                new BigDecimal("0.30")));

    mockMvc
        .perform(get("/api/v1/expenses/summary").param("month", "2026-03"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.month").value("2026-03"))
        .andExpect(jsonPath("$.total").value(0.30));
  }

  @Test
  void theSummaryEndpointRequiresAMonth() throws Exception {
    mockMvc
        .perform(get("/api/v1/expenses/summary"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("month"));
  }

  @Test
  void theListPassesEveryFilterToTheService() throws Exception {
    when(expenseService.listExpenses(any(), any(), any(), any(Pageable.class)))
        .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true));

    mockMvc
        .perform(
            get("/api/v1/expenses")
                .param("from", "2026-03-01")
                .param("to", "2026-03-31")
                .param("category", "FOOD"))
        .andExpect(status().isOk());

    verify(expenseService)
        .listExpenses(
            eq(LocalDate.of(2026, 3, 1)),
            eq(LocalDate.of(2026, 3, 31)),
            eq(ExpenseCategory.FOOD),
            any(Pageable.class));
  }

  @Test
  void anExpenseThatDoesNotExistReturnsNotFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(expenseService.getExpense(id)).thenThrow(new ExpenseNotFoundException(id));

    mockMvc
        .perform(get("/api/v1/expenses/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("EXPENSE_NOT_FOUND"));
  }
}
