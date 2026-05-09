package kz.logisto.lgwarehouseservice.config.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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
      OAuth2AuthorizedClientService authorizedClientService) {
    AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientService);
    manager.setAuthorizedClientProvider(
        OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build());
    return manager;
  }

  @Bean
  public ClientHttpRequestInterceptor httpSecurityInterceptor(
      OAuth2AuthorizedClientManager clientManager) {
    OAuth2ClientHttpRequestInterceptor interceptor =
        new OAuth2ClientHttpRequestInterceptor(clientManager);
    interceptor.setPrincipalResolver(request -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      return auth != null ? auth
          : new UsernamePasswordAuthenticationToken("system", null, List.of());
    });
    return interceptor;
  }
}
