package com.edstem.interviewprep.booking.config;

import java.time.Duration;
import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(
    LocalTime dayStart, LocalTime dayEnd, Duration slotLength, Duration holdLength) {}
