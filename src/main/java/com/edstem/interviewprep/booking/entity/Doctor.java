package com.edstem.interviewprep.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "doctors")
public class Doctor {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "speciality", nullable = false, length = 100)
  private String speciality;

  protected Doctor() {}

  private Doctor(String name, String speciality) {
    this.name = name;
    this.speciality = speciality;
  }

  public static Doctor create(String name, String speciality) {
    return new Doctor(name, speciality);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSpeciality() {
    return speciality;
  }
}
