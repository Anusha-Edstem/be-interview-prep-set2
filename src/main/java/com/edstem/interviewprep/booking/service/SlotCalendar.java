package com.edstem.interviewprep.booking.service;

import com.edstem.interviewprep.booking.config.BookingProperties;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SlotCalendar {

  private final BookingProperties properties;

  public SlotCalendar(BookingProperties properties) {
    this.properties = properties;
  }

  public List<Instant> slotsOn(LocalDate day) {
    List<Instant> slots = new ArrayList<>();
    Instant dayEnd = at(day, properties.dayEnd());
    Instant candidate = at(day, properties.dayStart());
    while (candidate.isBefore(dayEnd)) {
      slots.add(candidate);
      candidate = candidate.plus(properties.slotLength());
    }
    return slots;
  }

  public boolean isSlotStart(LocalDate day, Instant candidate) {
    return slotsOn(day).contains(candidate);
  }

  public LocalDate dayOf(Instant moment) {
    return LocalDateTime.ofInstant(moment, ZoneOffset.UTC).toLocalDate();
  }

  public Instant startOfDay(LocalDate day) {
    return day.atStartOfDay(ZoneOffset.UTC).toInstant();
  }

  public Instant startOfNextDay(LocalDate day) {
    return startOfDay(day.plusDays(1));
  }

  private Instant at(LocalDate day, java.time.LocalTime time) {
    return LocalDateTime.of(day, time).toInstant(ZoneOffset.UTC);
  }
}
