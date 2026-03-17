package kz.logisto.lgwarehouseservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class CurrencySecurityConfig extends AbstractSecurityConfig {

  @Bean
  public SecurityFilterChain currencyFilterChain(HttpSecurity http) throws Exception {
    super.init(http);
    return http
        .securityMatcher("/currencies/**")
        .authorizeHttpRequests(authorize ->
            authorize
                .anyRequest().authenticated())
        .build();
  }
}
