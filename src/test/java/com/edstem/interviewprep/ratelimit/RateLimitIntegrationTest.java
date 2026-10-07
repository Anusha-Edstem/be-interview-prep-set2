package com.edstem.interviewprep.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIntegrationTest {

  private static final String API_KEY_HEADER = "X-API-Key";

  @Autowired private MockMvc mockMvc;

  @Test
  void theEleventhRequestInAMinuteIsRejected() throws Exception {
    String apiKey = freshKey();

    for (int request = 1; request <= 10; request++) {
      mockMvc
          .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, apiKey))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.text").exists())
          .andExpect(header().string("X-RateLimit-Limit", "10"))
          .andExpect(header().string("X-RateLimit-Remaining", String.valueOf(10 - request)));
    }

    mockMvc
        .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, apiKey))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
        .andExpect(header().exists("Retry-After"))
        .andExpect(header().string("X-RateLimit-Remaining", "0"));
  }

  @Test
  void theRetryAfterHeaderTellsTheClientHowLongToWait() throws Exception {
    String apiKey = freshKey();
    for (int request = 1; request <= 10; request++) {
      mockMvc
          .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, apiKey))
          .andExpect(status().isOk());
    }

    String retryAfter =
        mockMvc
            .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, apiKey))
            .andExpect(status().isTooManyRequests())
            .andReturn()
            .getResponse()
            .getHeader("Retry-After");

    long seconds = Long.parseLong(retryAfter);
    org.assertj.core.api.Assertions.assertThat(seconds).isBetween(1L, 60L);
  }

  @Test
  void oneClientExhaustingItsAllowanceDoesNotBlockAnother() throws Exception {
    String exhausted = freshKey();
    String untouched = freshKey();

    for (int request = 1; request <= 10; request++) {
      mockMvc
          .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, exhausted))
          .andExpect(status().isOk());
    }
    mockMvc
        .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, exhausted))
        .andExpect(status().isTooManyRequests());

    mockMvc
        .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, untouched))
        .andExpect(status().isOk())
        .andExpect(header().string("X-RateLimit-Remaining", "9"));
  }

  @Test
  void aRequestWithoutAnApiKeyIsRejected() throws Exception {
    mockMvc
        .perform(get("/api/v1/quotes/random"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("API_KEY_REQUIRED"));
  }

  private String freshKey() {
    return "client-" + UUID.randomUUID();
  }
}
