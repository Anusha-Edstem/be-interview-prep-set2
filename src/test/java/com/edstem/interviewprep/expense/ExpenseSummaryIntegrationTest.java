package com.edstem.interviewprep.expense;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.interviewprep.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseSummaryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ExpenseRepository expenseRepository;

  @BeforeEach
  void clearLedger() {
    expenseRepository.deleteAll();
  }

  @Test
  void theMonthlySummaryIncludesExpensesOnTheFirstAndLastDayOfTheMonth() throws Exception {
    addExpense("10.00", "FOOD", "2026-03-01");
    addExpense("20.00", "FOOD", "2026-03-31");
    addExpense("99.00", "FOOD", "2026-02-28");
    addExpense("77.00", "FOOD", "2026-04-01");

    mockMvc
        .perform(get("/api/v1/expenses/summary").param("month", "2026-03"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.from").value("2026-03-01"))
        .andExpect(jsonPath("$.to").value("2026-03-31"))
        .andExpect(jsonPath("$.totalPerCategory.FOOD").value(30.00))
        .andExpect(jsonPath("$.total").value(30.00));
  }

  @Test
  void fractionalAmountsAddUpExactly() throws Exception {
    addExpense("0.10", "FOOD", "2026-03-05");
    addExpense("0.20", "TRAVEL", "2026-03-06");

    mockMvc
        .perform(get("/api/v1/expenses/summary").param("month", "2026-03"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPerCategory.FOOD").value(0.10))
        .andExpect(jsonPath("$.totalPerCategory.TRAVEL").value(0.20))
        .andExpect(jsonPath("$.totalPerCategory.BILLS").value(0.00))
        .andExpect(jsonPath("$.total").value(0.30));
  }

  @Test
  void aMonthWithNoSpendingSummarisesToZero() throws Exception {
    addExpense("15.00", "BILLS", "2026-05-10");

    mockMvc
        .perform(get("/api/v1/expenses/summary").param("month", "2026-06"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(0.00));
  }

  @Test
  void theListIsFilteredByDateRangeAndCategory() throws Exception {
    addExpense("10.00", "FOOD", "2026-03-01");
    addExpense("20.00", "TRAVEL", "2026-03-15");
    addExpense("30.00", "FOOD", "2026-04-02");

    mockMvc
        .perform(get("/api/v1/expenses").param("from", "2026-03-01").param("to", "2026-03-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));

    mockMvc
        .perform(
            get("/api/v1/expenses")
                .param("from", "2026-03-01")
                .param("to", "2026-03-31")
                .param("category", "TRAVEL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].amount").value(20.00));
  }

  @Test
  void anAmountWithMoreThanTwoDecimalPlacesIsRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"amount":10.123,"category":"FOOD","spentOn":"2026-03-01"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("amount"));
  }

  @Test
  void anAmountOfZeroIsRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"amount":0.00,"category":"FOOD","spentOn":"2026-03-01"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].message").value("must be greater than zero"));
  }

  private void addExpense(String amount, String category, String spentOn) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"amount":%s,"category":"%s","spentOn":"%s"}
                    """
                        .formatted(amount, category, spentOn)))
        .andExpect(status().isCreated());
  }
}
