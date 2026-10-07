package com.edstem.interviewprep.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.edstem.interviewprep.booking.dto.request.CreateDoctorRequest;
import com.edstem.interviewprep.booking.dto.request.HoldSlotRequest;
import com.edstem.interviewprep.booking.dto.response.DoctorResponse;
import com.edstem.interviewprep.booking.entity.Appointment;
import com.edstem.interviewprep.booking.entity.AppointmentStatus;
import com.edstem.interviewprep.booking.exception.SlotUnavailableException;
import com.edstem.interviewprep.booking.repository.AppointmentRepository;
import com.edstem.interviewprep.booking.repository.DoctorRepository;
import com.edstem.interviewprep.booking.service.BookingService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(MutableClockConfig.class)
class ConcurrentBookingTest {

  private static final int PATIENTS = 20;
  private static final Instant SLOT = Instant.parse("2026-03-02T09:00:00Z");

  @Autowired private BookingService bookingService;

  @Autowired private DoctorRepository doctorRepository;

  @Autowired private AppointmentRepository appointmentRepository;

  @Autowired private MutableClock clock;

  private UUID doctorId;

  @BeforeEach
  void reset() {
    appointmentRepository.deleteAll();
    doctorRepository.deleteAll();
    clock.resetTo(MutableClockConfig.FIXED_START);
    DoctorResponse doctor =
        bookingService.registerDoctor(new CreateDoctorRequest("Dr Who", "General"));
    doctorId = doctor.id();
  }

  @Test
  void exactlyOneOfTwentySimultaneousPatientsGetsTheSlot() throws Exception {
    AtomicInteger succeeded = new AtomicInteger();
    AtomicInteger refused = new AtomicInteger();
    CountDownLatch startLine = new CountDownLatch(1);

    ExecutorService pool = Executors.newFixedThreadPool(PATIENTS);
    try {
      List<Callable<Void>> patients =
          IntStream.range(0, PATIENTS)
              .<Callable<Void>>mapToObj(
                  index ->
                      () -> {
                        startLine.await();
                        try {
                          bookingService.holdSlot(
                              new HoldSlotRequest(doctorId, "patient-" + index, SLOT));
                          succeeded.incrementAndGet();
                        } catch (SlotUnavailableException refusedForThisPatient) {
                          refused.incrementAndGet();
                        }
                        return null;
                      })
              .toList();
      List<Future<Void>> running = patients.stream().map(pool::submit).toList();
      startLine.countDown();
      for (Future<Void> patient : running) {
        patient.get(60, TimeUnit.SECONDS);
      }
    } finally {
      pool.shutdownNow();
    }

    assertThat(succeeded.get()).isEqualTo(1);
    assertThat(refused.get()).isEqualTo(PATIENTS - 1);

    List<Appointment> active =
        appointmentRepository.findActiveForDoctorBetween(
            doctorId, Instant.parse("2026-03-02T00:00:00Z"), Instant.parse("2026-03-03T00:00:00Z"));
    assertThat(active).hasSize(1);
    assertThat(active.get(0).getStatus()).isEqualTo(AppointmentStatus.HELD);
  }

  @Test
  void twentyPatientsSpreadOverTwentySlotsAllSucceed() throws Exception {
    AtomicInteger succeeded = new AtomicInteger();
    CountDownLatch startLine = new CountDownLatch(1);
    int slots = 16;

    ExecutorService pool = Executors.newFixedThreadPool(slots);
    try {
      List<Callable<Void>> patients =
          IntStream.range(0, slots)
              .<Callable<Void>>mapToObj(
                  index ->
                      () -> {
                        startLine.await();
                        bookingService.holdSlot(
                            new HoldSlotRequest(
                                doctorId, "patient-" + index, SLOT.plusSeconds(index * 30L * 60L)));
                        succeeded.incrementAndGet();
                        return null;
                      })
              .toList();
      List<Future<Void>> running = patients.stream().map(pool::submit).toList();
      startLine.countDown();
      for (Future<Void> patient : running) {
        patient.get(60, TimeUnit.SECONDS);
      }
    } finally {
      pool.shutdownNow();
    }

    assertThat(succeeded.get()).isEqualTo(slots);
  }
}
