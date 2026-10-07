package com.edstem.interviewprep.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.interviewprep.booking.repository.AppointmentRepository;
import com.edstem.interviewprep.booking.repository.DoctorRepository;
import com.edstem.interviewprep.booking.service.AppointmentConfirmedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@SpringBootTest
@AutoConfigureMockMvc
@Import({MutableClockConfig.class, BookingLifecycleTest.ListenerConfig.class})
class BookingLifecycleTest {

  private static final String SLOT = "2026-03-02T09:00:00Z";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private MutableClock clock;

  @Autowired private DoctorRepository doctorRepository;

  @Autowired private AppointmentRepository appointmentRepository;

  @Autowired private RecordingListener listener;

  @BeforeEach
  void reset() {
    appointmentRepository.deleteAll();
    doctorRepository.deleteAll();
    clock.resetTo(MutableClockConfig.FIXED_START);
    listener.confirmed.clear();
  }

  @Test
  void aPatientSeesAvailableSlotsHoldsOneAndConfirmsIt() throws Exception {
    String doctorId = registerDoctor();

    mockMvc
        .perform(get("/api/v1/doctors/{id}/slots", doctorId).param("date", "2026-03-02"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.availableSlots.length()").value(16))
        .andExpect(jsonPath("$.availableSlots[0]").value("2026-03-02T09:00:00Z"));

    String appointmentId = holdSlot(doctorId, "patient-1", SLOT);

    mockMvc
        .perform(get("/api/v1/doctors/{id}/slots", doctorId).param("date", "2026-03-02"))
        .andExpect(jsonPath("$.availableSlots.length()").value(15));

    mockMvc
        .perform(post("/api/v1/appointments/{id}/confirm", appointmentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
        .andExpect(jsonPath("$.confirmedAt").exists());
  }

  @Test
  void aHoldThatIsNotConfirmedInTimeReleasesTheSlot() throws Exception {
    String doctorId = registerDoctor();
    String appointmentId = holdSlot(doctorId, "patient-1", SLOT);

    mockMvc
        .perform(get("/api/v1/doctors/{id}/slots", doctorId).param("date", "2026-03-02"))
        .andExpect(jsonPath("$.availableSlots.length()").value(15));

    clock.advance(Duration.ofMinutes(5));

    mockMvc
        .perform(get("/api/v1/doctors/{id}/slots", doctorId).param("date", "2026-03-02"))
        .andExpect(jsonPath("$.availableSlots.length()").value(16))
        .andExpect(jsonPath("$.availableSlots[0]").value(SLOT));

    mockMvc
        .perform(post("/api/v1/appointments/{id}/confirm", appointmentId))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("HOLD_EXPIRED"));

    String secondHold = holdSlot(doctorId, "patient-2", SLOT);
    mockMvc
        .perform(post("/api/v1/appointments/{id}/confirm", secondHold))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.patientId").value("patient-2"));
  }

  @Test
  void aHoldSurvivesUntilTheMomentItExpires() throws Exception {
    String doctorId = registerDoctor();
    String appointmentId = holdSlot(doctorId, "patient-1", SLOT);

    clock.advance(Duration.ofMinutes(4).plusSeconds(59));

    mockMvc
        .perform(post("/api/v1/appointments/{id}/confirm", appointmentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  void cancellingAConfirmedBookingFreesTheSlot() throws Exception {
    String doctorId = registerDoctor();
    String appointmentId = holdSlot(doctorId, "patient-1", SLOT);
    mockMvc
        .perform(post("/api/v1/appointments/{id}/confirm", appointmentId))
        .andExpect(status().isOk());

    mockMvc
        .perform(post("/api/v1/appointments/{id}/cancel", appointmentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"));

    mockMvc
        .perform(get("/api/v1/doctors/{id}/slots", doctorId).param("date", "2026-03-02"))
        .andExpect(jsonPath("$.availableSlots.length()").value(16));

    String rebooked = holdSlot(doctorId, "patient-2", SLOT);
    mockMvc.perform(post("/api/v1/appointments/{id}/confirm", rebooked)).andExpect(status().isOk());
  }

  @Test
  void aSlotHeldByOnePatientCannotBeHeldByAnother() throws Exception {
    String doctorId = registerDoctor();
    holdSlot(doctorId, "patient-1", SLOT);

    mockMvc
        .perform(
            post("/api/v1/appointments/holds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(holdBody(doctorId, "patient-2", SLOT)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SLOT_UNAVAILABLE"));
  }

  @Test
  void aTimeThatIsNotASlotStartIsRejected() throws Exception {
    String doctorId = registerDoctor();

    mockMvc
        .perform(
            post("/api/v1/appointments/holds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(holdBody(doctorId, "patient-1", "2026-03-02T09:15:00Z")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_SLOT"));
  }

  @Test
  void aNotificationIsSentOnlyAfterTheConfirmationIsCommitted() throws Exception {
    String doctorId = registerDoctor();
    String appointmentId = holdSlot(doctorId, "patient-1", SLOT);

    assertThat(listener.confirmed).isEmpty();

    mockMvc
        .perform(post("/api/v1/appointments/{id}/confirm", appointmentId))
        .andExpect(status().isOk());

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> assertThat(listener.confirmed).hasSize(1));
    assertThat(listener.confirmed.get(0).patientId()).isEqualTo("patient-1");
  }

  @Test
  void noNotificationIsSentWhenTheConfirmationFails() throws Exception {
    String doctorId = registerDoctor();
    String appointmentId = holdSlot(doctorId, "patient-1", SLOT);
    clock.advance(Duration.ofMinutes(5));

    mockMvc
        .perform(post("/api/v1/appointments/{id}/confirm", appointmentId))
        .andExpect(status().isConflict());

    Thread.sleep(300);
    assertThat(listener.confirmed).isEmpty();
  }

  private String registerDoctor() throws Exception {
    String payload =
        mockMvc
            .perform(
                post("/api/v1/doctors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Dr Who\",\"speciality\":\"General\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(payload).get("id").asText();
  }

  private String holdSlot(String doctorId, String patientId, String startsAt) throws Exception {
    String payload =
        mockMvc
            .perform(
                post("/api/v1/appointments/holds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(holdBody(doctorId, patientId, startsAt)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(payload).get("id").asText();
  }

  private String holdBody(String doctorId, String patientId, String startsAt) {
    return """
        {"doctorId":"%s","patientId":"%s","startsAt":"%s"}
        """
        .formatted(doctorId, patientId, startsAt);
  }

  @TestConfiguration
  static class ListenerConfig {

    @Bean
    RecordingListener recordingListener() {
      return new RecordingListener();
    }
  }

  static class RecordingListener {

    private final List<AppointmentConfirmedEvent> confirmed = new CopyOnWriteArrayList<>();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onConfirmed(AppointmentConfirmedEvent event) {
      confirmed.add(event);
    }
  }
}
