package com.edstem.interviewprep.booking.service;

import com.edstem.interviewprep.booking.config.BookingProperties;
import com.edstem.interviewprep.booking.dto.request.CreateDoctorRequest;
import com.edstem.interviewprep.booking.dto.request.HoldSlotRequest;
import com.edstem.interviewprep.booking.dto.response.AppointmentResponse;
import com.edstem.interviewprep.booking.dto.response.AvailableSlotsResponse;
import com.edstem.interviewprep.booking.dto.response.DoctorResponse;
import com.edstem.interviewprep.booking.entity.Appointment;
import com.edstem.interviewprep.booking.entity.Doctor;
import com.edstem.interviewprep.booking.exception.AppointmentNotFoundException;
import com.edstem.interviewprep.booking.exception.AppointmentStateException;
import com.edstem.interviewprep.booking.exception.DoctorNotFoundException;
import com.edstem.interviewprep.booking.exception.HoldExpiredException;
import com.edstem.interviewprep.booking.exception.InvalidSlotException;
import com.edstem.interviewprep.booking.exception.SlotUnavailableException;
import com.edstem.interviewprep.booking.mapper.BookingMapper;
import com.edstem.interviewprep.booking.repository.AppointmentRepository;
import com.edstem.interviewprep.booking.repository.DoctorRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

  private final DoctorRepository doctorRepository;
  private final AppointmentRepository appointmentRepository;
  private final SlotCalendar slotCalendar;
  private final BookingProperties properties;
  private final ApplicationEventPublisher events;
  private final java.time.Clock clock;

  public BookingService(
      DoctorRepository doctorRepository,
      AppointmentRepository appointmentRepository,
      SlotCalendar slotCalendar,
      BookingProperties properties,
      ApplicationEventPublisher events,
      java.time.Clock clock) {
    this.doctorRepository = doctorRepository;
    this.appointmentRepository = appointmentRepository;
    this.slotCalendar = slotCalendar;
    this.properties = properties;
    this.events = events;
    this.clock = clock;
  }

  @Transactional
  public DoctorResponse registerDoctor(CreateDoctorRequest request) {
    Doctor doctor = Doctor.create(request.name(), request.speciality());
    return BookingMapper.toResponse(doctorRepository.save(doctor));
  }

  @Transactional
  public AvailableSlotsResponse availableSlots(UUID doctorId, LocalDate date) {
    requireDoctor(doctorId);
    appointmentRepository.expireHoldsNotConfirmedBy(Instant.now(clock));

    Set<Instant> taken =
        appointmentRepository
            .findActiveForDoctorBetween(
                doctorId, slotCalendar.startOfDay(date), slotCalendar.startOfNextDay(date))
            .stream()
            .map(Appointment::getStartsAt)
            .collect(Collectors.toSet());

    List<Instant> free =
        slotCalendar.slotsOn(date).stream().filter(slot -> !taken.contains(slot)).toList();
    return new AvailableSlotsResponse(doctorId, date, free);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public AppointmentResponse holdSlot(HoldSlotRequest request) {
    requireDoctor(request.doctorId());
    Instant now = Instant.now(clock);
    LocalDate day = slotCalendar.dayOf(request.startsAt());
    if (!slotCalendar.isSlotStart(day, request.startsAt())) {
      throw new InvalidSlotException(request.startsAt());
    }

    appointmentRepository.expireHoldsNotConfirmedBy(now);

    Appointment held =
        Appointment.hold(
            request.doctorId(),
            request.patientId(),
            request.startsAt(),
            now.plus(properties.holdLength()),
            now);
    try {
      return BookingMapper.toResponse(appointmentRepository.saveAndFlush(held));
    } catch (DataIntegrityViolationException collision) {
      throw new SlotUnavailableException(request.startsAt());
    }
  }

  @Transactional
  public AppointmentResponse confirmHold(UUID appointmentId) {
    Instant now = Instant.now(clock);
    Appointment appointment = findAppointmentOrThrow(appointmentId);

    if (appointment.isConfirmed()) {
      return BookingMapper.toResponse(appointment);
    }
    if (appointment.isExpired()) {
      throw new HoldExpiredException(appointmentId);
    }
    if (!appointment.isHeld()) {
      throw new AppointmentStateException(appointmentId, appointment.getStatus(), "HELD");
    }
    if (appointment.hasHoldExpiredAt(now)) {
      appointment.expire();
      throw new HoldExpiredException(appointmentId);
    }

    appointment.confirm(now);
    events.publishEvent(
        new AppointmentConfirmedEvent(
            appointment.getId(),
            appointment.getDoctorId(),
            appointment.getPatientId(),
            appointment.getStartsAt()));
    return BookingMapper.toResponse(appointment);
  }

  @Transactional
  public AppointmentResponse cancel(UUID appointmentId) {
    Appointment appointment = findAppointmentOrThrow(appointmentId);
    if (!appointment.isConfirmed() && !appointment.isHeld()) {
      throw new AppointmentStateException(
          appointmentId, appointment.getStatus(), "HELD or CONFIRMED");
    }
    appointment.cancel();
    return BookingMapper.toResponse(appointment);
  }

  @Transactional(readOnly = true)
  public AppointmentResponse getAppointment(UUID appointmentId) {
    return BookingMapper.toResponse(findAppointmentOrThrow(appointmentId));
  }

  private void requireDoctor(UUID doctorId) {
    if (!doctorRepository.existsById(doctorId)) {
      throw new DoctorNotFoundException(doctorId);
    }
  }

  private Appointment findAppointmentOrThrow(UUID id) {
    return appointmentRepository
        .findById(id)
        .orElseThrow(() -> new AppointmentNotFoundException(id));
  }
}
