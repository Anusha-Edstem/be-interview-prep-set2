package com.edstem.interviewprep.expense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "expenses")
public class Expense {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 20)
  private ExpenseCategory category;

  @Column(name = "spent_on", nullable = false)
  private LocalDate spentOn;

  @Column(name = "note", length = 500)
  private String note;

  @CreationTimestamp
  @Column(name = "created_date", nullable = false, updatable = false)
  private Instant createdDate;

  protected Expense() {}

  private Expense(BigDecimal amount, ExpenseCategory category, LocalDate spentOn, String note) {
    this.amount = amount;
    this.category = category;
    this.spentOn = spentOn;
    this.note = note;
  }

  public static Expense create(
      BigDecimal amount, ExpenseCategory category, LocalDate spentOn, String note) {
    return new Expense(amount, category, spentOn, note);
  }

  public void update(BigDecimal amount, ExpenseCategory category, LocalDate spentOn, String note) {
    this.amount = amount;
    this.category = category;
    this.spentOn = spentOn;
    this.note = note;
  }

  public UUID getId() {
    return id;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public ExpenseCategory getCategory() {
    return category;
  }

  public LocalDate getSpentOn() {
    return spentOn;
  }

  public String getNote() {
    return note;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }
}
