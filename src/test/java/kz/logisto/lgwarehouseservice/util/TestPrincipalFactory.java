package kz.logisto.lgwarehouseservice.util;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

public final class TestPrincipalFactory {

  private TestPrincipalFactory() {
  }

  public static Principal create() {
    return create("test-user");
  }

  public static Principal create(String name) {
    OAuth2AuthenticatedPrincipal authPrincipal = new DefaultOAuth2AuthenticatedPrincipal(
        name, Map.of("sub", name), null);
    OAuth2AccessToken token = new OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER, "tokenValue",
        Instant.now(), Instant.now().plusSeconds(300));
    return new BearerTokenAuthentication(authPrincipal, token, null);
  }
}
