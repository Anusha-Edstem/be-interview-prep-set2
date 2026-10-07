package com.edstem.interviewprep.ratelimit.config;

import com.edstem.interviewprep.ratelimit.service.RateLimiter;
import com.edstem.interviewprep.ratelimit.web.RateLimitInterceptor;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig implements WebMvcConfigurer {

  private final RateLimitProperties properties;
  private final RateLimiter rateLimiter;

  public RateLimitConfig(RateLimitProperties properties, ObjectProvider<Clock> clock) {
    this.properties = properties;
    this.rateLimiter = new RateLimiter(properties, clock.getIfAvailable(Clock::systemUTC));
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(new RateLimitInterceptor(rateLimiter, properties))
        .addPathPatterns("/api/v1/quotes/**");
  }
}
