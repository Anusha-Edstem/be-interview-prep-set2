package com.edstem.interviewprep.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

public class MutableClock extends Clock {

  private final AtomicReference<Instant> now;

  public MutableClock(Instant startingAt) {
    this.now = new AtomicReference<>(startingAt);
  }

  public void advance(Duration amount) {
    now.updateAndGet(current -> current.plus(amount));
  }

  public void resetTo(Instant moment) {
    now.set(moment);
  }

  @Override
  public ZoneId getZone() {
    return ZoneOffset.UTC;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }

  @Override
  public Instant instant() {
    return now.get();
  }
}
