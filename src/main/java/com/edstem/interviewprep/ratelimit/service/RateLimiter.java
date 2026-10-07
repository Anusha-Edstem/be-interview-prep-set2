package com.edstem.interviewprep.ratelimit.service;

import com.edstem.interviewprep.ratelimit.config.RateLimitProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class RateLimiter {

  private final Map<String, Window> windows = new ConcurrentHashMap<>();
  private final RateLimitProperties properties;
  private final Clock clock;

  public RateLimiter(RateLimitProperties properties, Clock clock) {
    this.properties = properties;
    this.clock = clock;
  }

  public RateLimitDecision tryAcquire(String clientKey) {
    Instant now = Instant.now(clock);
    Duration window = properties.window();
    int limit = properties.limit();
    AtomicReference<RateLimitDecision> decision = new AtomicReference<>();

    windows.compute(
        clientKey,
        (ignored, current) -> {
          Window active =
              current == null || current.hasExpiredAt(now, window) ? new Window(now, 0) : current;
          if (active.count() >= limit) {
            decision.set(RateLimitDecision.rejected(active.retryAfterFrom(now, window)));
            return active;
          }
          decision.set(RateLimitDecision.allowed(limit - active.count() - 1));
          return active.incremented();
        });

    return decision.get();
  }

  public int limit() {
    return properties.limit();
  }

  private record Window(Instant startedAt, int count) {

    boolean hasExpiredAt(Instant now, Duration length) {
      return !now.isBefore(startedAt.plus(length));
    }

    Duration retryAfterFrom(Instant now, Duration length) {
      Duration remaining = Duration.between(now, startedAt.plus(length));
      return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    Window incremented() {
      return new Window(startedAt, count + 1);
    }
  }
}
