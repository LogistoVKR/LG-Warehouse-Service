package kz.logisto.lgwarehouseservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class OzonSecurityConfig extends AbstractSecurityConfig {

  @Bean
  public SecurityFilterChain ozonFilterChain(HttpSecurity http) throws Exception {
    super.init(http);
    return http
        .securityMatcher("/ozon/**")
        .authorizeHttpRequests(authorize ->
            authorize.anyRequest().authenticated())
        .build();
  }
}
