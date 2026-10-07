package com.edstem.interviewprep.ratelimit.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.edstem.interviewprep.ratelimit.config.RateLimitProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

  private static final Instant START = Instant.parse("2026-03-01T10:00:00Z");

  @Test
  void theConfiguredNumberOfRequestsIsAllowedAndTheNextOneIsNot() {
    RateLimiter limiter = limiterAt(START, 10, Duration.ofMinutes(1));

    for (int request = 1; request <= 10; request++) {
      assertThat(limiter.tryAcquire("key-a").allowed()).as("request %d", request).isTrue();
    }

    assertThat(limiter.tryAcquire("key-a").allowed()).isFalse();
  }

  @Test
  void theRemainingAllowanceCountsDownToZero() {
    RateLimiter limiter = limiterAt(START, 3, Duration.ofMinutes(1));

    assertThat(limiter.tryAcquire("key-a").remaining()).isEqualTo(2);
    assertThat(limiter.tryAcquire("key-a").remaining()).isEqualTo(1);
    assertThat(limiter.tryAcquire("key-a").remaining()).isZero();
  }

  @Test
  void aRejectedRequestReportsHowLongToWait() {
    MutableClock clock = new MutableClock(START);
    RateLimiter limiter = new RateLimiter(properties(2, Duration.ofMinutes(1)), clock);
    limiter.tryAcquire("key-a");
    limiter.tryAcquire("key-a");

    clock.advance(Duration.ofSeconds(20));
    RateLimitDecision rejected = limiter.tryAcquire("key-a");

    assertThat(rejected.allowed()).isFalse();
    assertThat(rejected.retryAfter()).isEqualTo(Duration.ofSeconds(40));
  }

  @Test
  void theAllowanceIsRestoredOnceTheWindowHasPassed() {
    MutableClock clock = new MutableClock(START);
    RateLimiter limiter = new RateLimiter(properties(2, Duration.ofMinutes(1)), clock);
    limiter.tryAcquire("key-a");
    limiter.tryAcquire("key-a");
    assertThat(limiter.tryAcquire("key-a").allowed()).isFalse();

    clock.advance(Duration.ofSeconds(60));

    assertThat(limiter.tryAcquire("key-a").allowed()).isTrue();
  }

  @Test
  void theWindowIsNotRestoredAMomentBeforeItExpires() {
    MutableClock clock = new MutableClock(START);
    RateLimiter limiter = new RateLimiter(properties(1, Duration.ofMinutes(1)), clock);
    limiter.tryAcquire("key-a");

    clock.advance(Duration.ofSeconds(59));

    assertThat(limiter.tryAcquire("key-a").allowed()).isFalse();
  }

  @Test
  void oneClientUsingItsAllowanceDoesNotAffectAnother() {
    RateLimiter limiter = limiterAt(START, 2, Duration.ofMinutes(1));
    limiter.tryAcquire("key-a");
    limiter.tryAcquire("key-a");
    assertThat(limiter.tryAcquire("key-a").allowed()).isFalse();

    assertThat(limiter.tryAcquire("key-b").allowed()).isTrue();
    assertThat(limiter.tryAcquire("key-b").allowed()).isTrue();
    assertThat(limiter.tryAcquire("key-b").allowed()).isFalse();
  }

  @Test
  void aDifferentConfiguredLimitChangesHowManyRequestsPass() {
    RateLimiter limiter = limiterAt(START, 1, Duration.ofMinutes(1));

    assertThat(limiter.tryAcquire("key-a").allowed()).isTrue();
    assertThat(limiter.tryAcquire("key-a").allowed()).isFalse();
  }

  private RateLimiter limiterAt(Instant now, int limit, Duration window) {
    return new RateLimiter(properties(limit, window), Clock.fixed(now, ZoneOffset.UTC));
  }

  private RateLimitProperties properties(int limit, Duration window) {
    return new RateLimitProperties(limit, window, "X-API-Key");
  }

  private static final class MutableClock extends Clock {

    private Instant now;

    private MutableClock(Instant now) {
      this.now = now;
    }

    private void advance(Duration amount) {
      now = now.plus(amount);
    }

    @Override
    public ZoneOffset getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(java.time.ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }
}
