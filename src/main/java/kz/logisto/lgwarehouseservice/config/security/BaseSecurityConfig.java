package kz.logisto.lgwarehouseservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BaseSecurityConfig extends AbstractSecurityConfig {

  private static final String[] PERMIT_ALL_PATHS = new String[]{
      "/actuator/**",
      "/swagger-ui/**",
      "/v3/api-docs/**",
  };

  @Bean
  public SecurityFilterChain baseFilterChain(HttpSecurity http)
      throws Exception {
    super.init(http);
    return http
        .securityMatcher(PERMIT_ALL_PATHS)
        .authorizeHttpRequests(authorize ->
            authorize
                .anyRequest().permitAll())
        .build();
  }

  @Bean
  public OAuth2AuthorizedClientManager clientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientRepository authorizedClientRepository) {
    return new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
        authorizedClientRepository);
  }

  @Bean
  public ClientHttpRequestInterceptor httpSecurityInterceptor(
      OAuth2AuthorizedClientManager clientManager) {
    return new OAuth2ClientHttpRequestInterceptor(clientManager);
  }
}
