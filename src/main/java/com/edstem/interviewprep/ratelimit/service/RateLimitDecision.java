package com.edstem.interviewprep.ratelimit.service;

import java.time.Duration;

public record RateLimitDecision(boolean allowed, int remaining, Duration retryAfter) {

  public static RateLimitDecision allowed(int remaining) {
    return new RateLimitDecision(true, remaining, Duration.ZERO);
  }

  public static RateLimitDecision rejected(Duration retryAfter) {
    return new RateLimitDecision(false, 0, retryAfter);
  }
}
