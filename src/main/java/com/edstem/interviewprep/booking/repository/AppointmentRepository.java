package com.edstem.interviewprep.booking.repository;

import com.edstem.interviewprep.booking.entity.Appointment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

  @Query(
      """
      select a from Appointment a
      where a.doctorId = :doctorId
        and a.startsAt >= :dayStart
        and a.startsAt < :dayEnd
        and a.activeSlot is not null
      """)
  List<Appointment> findActiveForDoctorBetween(
      @Param("doctorId") UUID doctorId,
      @Param("dayStart") Instant dayStart,
      @Param("dayEnd") Instant dayEnd);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update Appointment a
      set a.status = com.edstem.interviewprep.booking.entity.AppointmentStatus.EXPIRED,
          a.activeSlot = null,
          a.heldUntil = null
      where a.status = com.edstem.interviewprep.booking.entity.AppointmentStatus.HELD
        and a.heldUntil <= :now
      """)
  int expireHoldsNotConfirmedBy(@Param("now") Instant now);
}
