package kz.logisto.lgwarehouseservice.config.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

public abstract class AbstractSecurityConfig {

  protected final void init(HttpSecurity http)
      throws Exception {
    http
        .oauth2ResourceServer(
            oauth -> oauth.jwt(Customizer.withDefaults()))
        .cors(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable);
  }
}
