package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.model.PointOfStorageModel;
import kz.logisto.lgwarehouseservice.data.repository.PointOfStorageRepository;
import kz.logisto.lgwarehouseservice.mapper.PointOfStorageMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.PointOfStorageService;
import kz.logisto.lgwarehouseservice.service.impl.PointOfStorageServiceImpl;
import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

@ExtendWith(MockitoExtension.class)
public class PointOfStorageTest {

  @Mock
  private AccessService accessService;

  @Mock
  private PointOfStorageMapper mapper;

  @Mock
  private PointOfStorageRepository repository;

  private PointOfStorageService service;

  @BeforeEach
  public void init() {
    this.service = new PointOfStorageServiceImpl(accessService, mapper, repository);
  }

  @Test
  void findByIdTest() {
    Principal principal = principal();
    PointOfStorage pointOfStorage = new PointOfStorage();
    pointOfStorage.setId(UUID.randomUUID());
    pointOfStorage.setOrganizationId(UUID.randomUUID());
    PointOfStorageModel pointOfStorageModel = new PointOfStorageModel();

    when(repository.findById(pointOfStorage.getId()))
        .thenReturn(Optional.of(pointOfStorage));
    when(mapper.toModel(pointOfStorage))
        .thenReturn(pointOfStorageModel);

    PointOfStorageModel model = assertDoesNotThrow(
        () -> service.findById(pointOfStorage.getId(), principal));

    assertNotNull(model);

    verify(accessService, times(1))
        .isMemberOrThrow(principal.getName(), pointOfStorage.getOrganizationId());
    verify(repository, times(1))
        .findById(pointOfStorage.getId());
    verify(mapper, times(1))
        .toModel(pointOfStorage);
  }

  private Principal principal() {
    OAuth2AuthenticatedPrincipal authenticatedPrincipal = new DefaultOAuth2AuthenticatedPrincipal(
        "name",
        Map.of("map", "map"), null);
    OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
        "tokenValue", Instant.now(), Instant.now().plusSeconds(300));
    return new BearerTokenAuthentication(authenticatedPrincipal, token, null);
  }
}
