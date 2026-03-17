package kz.logisto.lgwarehouseservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class PointOfStorageSecurityConfig extends AbstractSecurityConfig {

  @Bean
  public SecurityFilterChain pointOfStorageFilterChain(HttpSecurity http) throws Exception {
    super.init(http);
    return http
        .securityMatcher("/points-of-storage/**")
        .authorizeHttpRequests(authorize ->
            authorize
                .anyRequest().authenticated())
        .build();
  }
}
