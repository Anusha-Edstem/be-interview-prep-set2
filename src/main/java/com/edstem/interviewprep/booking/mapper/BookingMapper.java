package com.edstem.interviewprep.booking.mapper;

import com.edstem.interviewprep.booking.dto.response.AppointmentResponse;
import com.edstem.interviewprep.booking.dto.response.DoctorResponse;
import com.edstem.interviewprep.booking.entity.Appointment;
import com.edstem.interviewprep.booking.entity.Doctor;

public final class BookingMapper {

  private BookingMapper() {}

  public static DoctorResponse toResponse(Doctor doctor) {
    return new DoctorResponse(doctor.getId(), doctor.getName(), doctor.getSpeciality());
  }

  public static AppointmentResponse toResponse(Appointment appointment) {
    return new AppointmentResponse(
        appointment.getId(),
        appointment.getDoctorId(),
        appointment.getPatientId(),
        appointment.getStartsAt(),
        appointment.getStatus(),
        appointment.getHeldUntil(),
        appointment.getConfirmedAt());
  }
}
