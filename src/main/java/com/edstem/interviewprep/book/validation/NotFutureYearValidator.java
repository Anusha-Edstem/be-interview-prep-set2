package com.edstem.interviewprep.book.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.time.Year;

public class NotFutureYearValidator implements ConstraintValidator<NotFutureYear, Integer> {

  private final Clock clock;

  public NotFutureYearValidator() {
    this(Clock.systemUTC());
  }

  NotFutureYearValidator(Clock clock) {
    this.clock = clock;
  }

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext context) {
    return value == null || value <= Year.now(clock).getValue();
  }
}
