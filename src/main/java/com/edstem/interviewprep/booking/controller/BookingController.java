package com.edstem.interviewprep.booking.controller;

import com.edstem.interviewprep.booking.dto.request.CreateDoctorRequest;
import com.edstem.interviewprep.booking.dto.request.HoldSlotRequest;
import com.edstem.interviewprep.booking.dto.response.AppointmentResponse;
import com.edstem.interviewprep.booking.dto.response.AvailableSlotsResponse;
import com.edstem.interviewprep.booking.dto.response.DoctorResponse;
import com.edstem.interviewprep.booking.service.BookingService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BookingController {

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @PostMapping("/doctors")
  public ResponseEntity<DoctorResponse> registerDoctor(
      @Valid @RequestBody CreateDoctorRequest request) {
    DoctorResponse created = bookingService.registerDoctor(request);
    return ResponseEntity.created(URI.create("/api/v1/doctors/" + created.id())).body(created);
  }

  @GetMapping("/doctors/{doctorId}/slots")
  public ResponseEntity<AvailableSlotsResponse> availableSlots(
      @PathVariable UUID doctorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(bookingService.availableSlots(doctorId, date));
  }

  @PostMapping("/appointments/holds")
  public ResponseEntity<AppointmentResponse> holdSlot(@Valid @RequestBody HoldSlotRequest request) {
    AppointmentResponse held = bookingService.holdSlot(request);
    return ResponseEntity.created(URI.create("/api/v1/appointments/" + held.id())).body(held);
  }

  @PostMapping("/appointments/{id}/confirm")
  public ResponseEntity<AppointmentResponse> confirm(@PathVariable UUID id) {
    return ResponseEntity.ok(bookingService.confirmHold(id));
  }

  @PostMapping("/appointments/{id}/cancel")
  public ResponseEntity<AppointmentResponse> cancel(@PathVariable UUID id) {
    return ResponseEntity.ok(bookingService.cancel(id));
  }

  @GetMapping("/appointments/{id}")
  public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable UUID id) {
    return ResponseEntity.ok(bookingService.getAppointment(id));
  }
}
