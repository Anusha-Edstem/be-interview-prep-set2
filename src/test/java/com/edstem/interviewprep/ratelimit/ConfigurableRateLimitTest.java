package com.edstem.interviewprep.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.rate-limit.limit=3", "app.rate-limit.window=30s"})
@AutoConfigureMockMvc
class ConfigurableRateLimitTest {

  private static final String API_KEY_HEADER = "X-API-Key";

  @Autowired private MockMvc mockMvc;

  @Test
  void theLimitAndWindowComeFromConfigurationRatherThanCode() throws Exception {
    String apiKey = "client-" + UUID.randomUUID();

    for (int request = 1; request <= 3; request++) {
      mockMvc
          .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, apiKey))
          .andExpect(status().isOk())
          .andExpect(header().string("X-RateLimit-Limit", "3"));
    }

    mockMvc
        .perform(get("/api/v1/quotes/random").header(API_KEY_HEADER, apiKey))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string("Retry-After", org.hamcrest.Matchers.notNullValue()));
  }
}
