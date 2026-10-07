package com.edstem.interviewprep.quote.controller;

import com.edstem.interviewprep.quote.dto.response.QuoteResponse;
import com.edstem.interviewprep.quote.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

  private final QuoteService quoteService;

  public QuoteController(QuoteService quoteService) {
    this.quoteService = quoteService;
  }

  @GetMapping("/random")
  public ResponseEntity<QuoteResponse> randomQuote() {
    return ResponseEntity.ok(quoteService.randomQuote());
  }
}
