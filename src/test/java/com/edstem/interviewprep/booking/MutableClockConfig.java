package com.edstem.interviewprep.booking;

import java.time.Instant;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MutableClockConfig {

  public static final Instant FIXED_START = Instant.parse("2026-03-02T08:00:00Z");

  @Bean
  @Primary
  public MutableClock mutableClock() {
    return new MutableClock(FIXED_START);
  }
}
