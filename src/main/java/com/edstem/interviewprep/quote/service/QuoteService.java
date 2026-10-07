package com.edstem.interviewprep.quote.service;

import com.edstem.interviewprep.quote.dto.response.QuoteResponse;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

  private static final List<QuoteResponse> QUOTES =
      List.of(
          new QuoteResponse("Simplicity is the soul of efficiency.", "Austin Freeman"),
          new QuoteResponse("Premature optimisation is the root of all evil.", "Donald Knuth"),
          new QuoteResponse("Programs must be written for people to read.", "Harold Abelson"),
          new QuoteResponse("Make it work, make it right, make it fast.", "Kent Beck"),
          new QuoteResponse("Deleted code is debugged code.", "Jeff Sickel"));

  public QuoteResponse randomQuote() {
    return QUOTES.get(ThreadLocalRandom.current().nextInt(QUOTES.size()));
  }
}
