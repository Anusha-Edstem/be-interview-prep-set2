package com.edstem.interviewprep.ratelimit.web;

import com.edstem.interviewprep.ratelimit.config.RateLimitProperties;
import com.edstem.interviewprep.ratelimit.exception.ApiKeyRequiredException;
import com.edstem.interviewprep.ratelimit.exception.RateLimitExceededException;
import com.edstem.interviewprep.ratelimit.service.RateLimitDecision;
import com.edstem.interviewprep.ratelimit.service.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

  static final String LIMIT_HEADER = "X-RateLimit-Limit";
  static final String REMAINING_HEADER = "X-RateLimit-Remaining";
  static final String RETRY_AFTER_HEADER = "Retry-After";

  private final RateLimiter rateLimiter;
  private final RateLimitProperties properties;

  public RateLimitInterceptor(RateLimiter rateLimiter, RateLimitProperties properties) {
    this.rateLimiter = rateLimiter;
    this.properties = properties;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String clientKey = request.getHeader(properties.headerName());
    if (clientKey == null || clientKey.isBlank()) {
      throw new ApiKeyRequiredException(properties.headerName());
    }

    RateLimitDecision decision = rateLimiter.tryAcquire(clientKey);
    response.setHeader(LIMIT_HEADER, String.valueOf(rateLimiter.limit()));

    if (!decision.allowed()) {
      long retryAfterSeconds = Math.max(1, decision.retryAfter().toSeconds());
      response.setHeader(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
      response.setHeader(REMAINING_HEADER, "0");
      throw new RateLimitExceededException(rateLimiter.limit(), retryAfterSeconds);
    }

    response.setHeader(REMAINING_HEADER, String.valueOf(decision.remaining()));
    return true;
  }
}
