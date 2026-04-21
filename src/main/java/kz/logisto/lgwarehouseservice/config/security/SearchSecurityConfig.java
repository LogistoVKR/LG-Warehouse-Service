package kz.logisto.lgwarehouseservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SearchSecurityConfig extends AbstractSecurityConfig {

  @Bean
  public SecurityFilterChain searchFilterChain(HttpSecurity http) throws Exception {
    super.init(http);
    return http
        .securityMatcher("/search/**")
        .authorizeHttpRequests(authorize ->
            authorize
                .requestMatchers(HttpMethod.GET, "/search/items").permitAll()
                .anyRequest().authenticated())
        .build();
  }
}
