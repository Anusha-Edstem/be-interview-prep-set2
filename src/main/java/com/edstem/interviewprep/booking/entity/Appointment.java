package com.edstem.interviewprep.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "doctor_id", nullable = false, updatable = false)
  private UUID doctorId;

  @Column(name = "patient_id", nullable = false, length = 100, updatable = false)
  private String patientId;

  @Column(name = "starts_at", nullable = false, updatable = false)
  private Instant startsAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AppointmentStatus status;

  @Column(name = "active_slot", unique = true, length = 120)
  private String activeSlot;

  @Column(name = "held_until")
  private Instant heldUntil;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "confirmed_at")
  private Instant confirmedAt;

  protected Appointment() {}

  private Appointment(
      UUID doctorId, String patientId, Instant startsAt, Instant heldUntil, Instant createdAt) {
    this.doctorId = doctorId;
    this.patientId = patientId;
    this.startsAt = startsAt;
    this.status = AppointmentStatus.HELD;
    this.activeSlot = activeSlotKey(doctorId, startsAt);
    this.heldUntil = heldUntil;
    this.createdAt = createdAt;
  }

  public static Appointment hold(
      UUID doctorId, String patientId, Instant startsAt, Instant heldUntil, Instant createdAt) {
    return new Appointment(doctorId, patientId, startsAt, heldUntil, createdAt);
  }

  public static String activeSlotKey(UUID doctorId, Instant startsAt) {
    return doctorId + "@" + startsAt;
  }

  public boolean isHeld() {
    return status == AppointmentStatus.HELD;
  }

  public boolean isConfirmed() {
    return status == AppointmentStatus.CONFIRMED;
  }

  public boolean isExpired() {
    return status == AppointmentStatus.EXPIRED;
  }

  public boolean hasHoldExpiredAt(Instant now) {
    return status == AppointmentStatus.HELD && heldUntil != null && !now.isBefore(heldUntil);
  }

  public void confirm(Instant confirmedAt) {
    this.status = AppointmentStatus.CONFIRMED;
    this.confirmedAt = confirmedAt;
    this.heldUntil = null;
  }

  public void cancel() {
    this.status = AppointmentStatus.CANCELLED;
    this.activeSlot = null;
    this.heldUntil = null;
  }

  public void expire() {
    this.status = AppointmentStatus.EXPIRED;
    this.activeSlot = null;
    this.heldUntil = null;
  }

  public UUID getId() {
    return id;
  }

  public UUID getDoctorId() {
    return doctorId;
  }

  public String getPatientId() {
    return patientId;
  }

  public Instant getStartsAt() {
    return startsAt;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public Instant getHeldUntil() {
    return heldUntil;
  }

  public Instant getConfirmedAt() {
    return confirmedAt;
  }
}
