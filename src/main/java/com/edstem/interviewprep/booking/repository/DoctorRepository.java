package com.edstem.interviewprep.booking.repository;

import com.edstem.interviewprep.booking.entity.Doctor;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {}
