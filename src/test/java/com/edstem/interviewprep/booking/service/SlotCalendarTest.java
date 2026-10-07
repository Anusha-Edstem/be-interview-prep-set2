package com.edstem.interviewprep.booking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.edstem.interviewprep.booking.config.BookingProperties;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlotCalendarTest {

  private final SlotCalendar calendar =
      new SlotCalendar(
          new BookingProperties(
              LocalTime.of(9, 0),
              LocalTime.of(17, 0),
              Duration.ofMinutes(30),
              Duration.ofMinutes(5)));

  @Test
  void anEightHourDayIsDividedIntoThirtyMinuteSlots() {
    List<Instant> slots = calendar.slotsOn(LocalDate.of(2026, 3, 2));

    assertThat(slots).hasSize(16);
    assertThat(slots.get(0)).isEqualTo(Instant.parse("2026-03-02T09:00:00Z"));
    assertThat(slots.get(1)).isEqualTo(Instant.parse("2026-03-02T09:30:00Z"));
    assertThat(slots.get(15)).isEqualTo(Instant.parse("2026-03-02T16:30:00Z"));
  }

  @Test
  void theSlotAtTheEndOfTheDayIsNotOffered() {
    List<Instant> slots = calendar.slotsOn(LocalDate.of(2026, 3, 2));

    assertThat(slots).doesNotContain(Instant.parse("2026-03-02T17:00:00Z"));
  }

  @Test
  void aTimeOnTheHalfHourIsASlotStart() {
    assertThat(
            calendar.isSlotStart(LocalDate.of(2026, 3, 2), Instant.parse("2026-03-02T10:30:00Z")))
        .isTrue();
  }

  @Test
  void aTimeBetweenSlotsIsNotASlotStart() {
    assertThat(
            calendar.isSlotStart(LocalDate.of(2026, 3, 2), Instant.parse("2026-03-02T10:15:00Z")))
        .isFalse();
  }

  @Test
  void aTimeOutsideWorkingHoursIsNotASlotStart() {
    assertThat(
            calendar.isSlotStart(LocalDate.of(2026, 3, 2), Instant.parse("2026-03-02T08:00:00Z")))
        .isFalse();
    assertThat(
            calendar.isSlotStart(LocalDate.of(2026, 3, 2), Instant.parse("2026-03-02T17:30:00Z")))
        .isFalse();
  }
}
