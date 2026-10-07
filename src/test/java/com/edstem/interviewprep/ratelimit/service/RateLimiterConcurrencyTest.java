package com.edstem.interviewprep.ratelimit.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.edstem.interviewprep.ratelimit.config.RateLimitProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class RateLimiterConcurrencyTest {

  private static final int LIMIT = 10;
  private static final int CALLERS = 200;

  @Test
  void onlyTheConfiguredNumberOfRequestsPassesWhenManyArriveAtOnce() throws Exception {
    RateLimiter limiter =
        new RateLimiter(
            new RateLimitProperties(LIMIT, Duration.ofMinutes(1), "X-API-Key"),
            Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC));
    AtomicInteger allowed = new AtomicInteger();
    CountDownLatch startLine = new CountDownLatch(1);

    ExecutorService pool = Executors.newFixedThreadPool(32);
    try {
      List<Callable<Void>> callers =
          IntStream.range(0, CALLERS)
              .<Callable<Void>>mapToObj(
                  index ->
                      () -> {
                        startLine.await();
                        if (limiter.tryAcquire("same-key").allowed()) {
                          allowed.incrementAndGet();
                        }
                        return null;
                      })
              .toList();
      List<Future<Void>> running = callers.stream().map(pool::submit).toList();
      startLine.countDown();
      for (Future<Void> caller : running) {
        caller.get(30, TimeUnit.SECONDS);
      }
    } finally {
      pool.shutdownNow();
    }

    assertThat(allowed.get()).isEqualTo(LIMIT);
  }

  @Test
  void concurrentCallersOnDifferentKeysEachGetTheirOwnAllowance() throws Exception {
    RateLimiter limiter =
        new RateLimiter(
            new RateLimitProperties(LIMIT, Duration.ofMinutes(1), "X-API-Key"),
            Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC));
    AtomicInteger allowed = new AtomicInteger();
    CountDownLatch startLine = new CountDownLatch(1);
    int keys = 8;

    ExecutorService pool = Executors.newFixedThreadPool(32);
    try {
      List<Callable<Void>> callers =
          IntStream.range(0, keys * LIMIT)
              .<Callable<Void>>mapToObj(
                  index ->
                      () -> {
                        startLine.await();
                        if (limiter.tryAcquire("key-" + (index % keys)).allowed()) {
                          allowed.incrementAndGet();
                        }
                        return null;
                      })
              .toList();
      List<Future<Void>> running = callers.stream().map(pool::submit).toList();
      startLine.countDown();
      for (Future<Void> caller : running) {
        caller.get(30, TimeUnit.SECONDS);
      }
    } finally {
      pool.shutdownNow();
    }

    assertThat(allowed.get()).isEqualTo(keys * LIMIT);
  }
}
