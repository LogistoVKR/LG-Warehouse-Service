package kz.logisto.lgwarehouseservice.service.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import kz.logisto.lgwarehouseservice.config.property.RestProperty;
import kz.logisto.lgwarehouseservice.config.property.RestProperty.RestServiceProperty;
import kz.logisto.lgwarehouseservice.data.dto.PageResponse;
import kz.logisto.lgwarehouseservice.data.model.ClientMembershipModel;
import kz.logisto.lgwarehouseservice.data.model.OrganizationModel;
import kz.logisto.lgwarehouseservice.data.model.OzonApiKeyModel;
import kz.logisto.lgwarehouseservice.service.UserService;
import kz.logisto.lgwarehouseservice.util.RestClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

  private static final Consumer<Map<String, Object>> CLIENT_ATTRIBUTES =
      RequestAttributeClientRegistrationIdResolver.clientRegistrationId("keycloak");

  private final RestClient restClient;
  private final RestServiceProperty property;

  public UserServiceImpl(RestProperty restProperty,
      ClientHttpRequestInterceptor httpSecurityInterceptor) {
    this.restClient = RestClientUtil.build(restProperty.getMcUserService(),
        httpSecurityInterceptor);
    this.property = restProperty.getMcUserService();
  }

  @Override
  public boolean isMember(String userId, UUID organizationId) {
    URI uri = UriComponentsBuilder.newInstance()
        .path(property.getContextPath() + "/organizations/{organizationId}/membership")
        .queryParam("userId", userId)
        .build(organizationId);

    try {
      Boolean result = restClient.get()
          .uri(uri)
          .attributes(CLIENT_ATTRIBUTES)
          .retrieve()
          .body(Boolean.class);
      return result != null && result;
    } catch (HttpStatusCodeException exception) {
      log.error("Cannot check membership for user {} in organization {} -> status: {}; message: {}",
          userId, organizationId, exception.getStatusCode(), exception.getMessage());
    }
    return false;
  }

  @Override
  public boolean canManageWarehouse(String userId, UUID organizationId) {
    URI uri = UriComponentsBuilder.newInstance()
        .path(property.getContextPath() + "/organizations/{organizationId}/warehouse-access")
        .queryParam("userId", userId)
        .build(organizationId);

    try {
      Boolean result = restClient.get()
          .uri(uri)
          .attributes(CLIENT_ATTRIBUTES)
          .retrieve()
          .body(Boolean.class);
      return result != null && result;
    } catch (HttpStatusCodeException exception) {
      log.error(
          "Cannot check warehouse access for user {} in organization {} -> status: {}; message: {}",
          userId, organizationId, exception.getStatusCode(), exception.getMessage());
    }
    return false;
  }

  @Override
  public OzonApiKeyModel getOzonApiKeyByOrganizationId(UUID organizationId) {
    URI uri = UriComponentsBuilder.newInstance()
        .path(property.getContextPath() + "/organizations/{organizationId}/ozon-api-key")
        .build(organizationId);

    try {
      return restClient.get()
          .uri(uri)
          .attributes(CLIENT_ATTRIBUTES)
          .retrieve()
          .body(OzonApiKeyModel.class);
    } catch (HttpStatusCodeException exception) {
      log.error("Cannot get ozon api key for organization {} -> status: {}; message: {}",
          organizationId, exception.getStatusCode(), exception.getMessage());
    }
    return null;
  }

  @Override
  public Optional<BigDecimal> getClientPersonalDiscount(UUID organizationId, UUID clientId) {
    URI uri = UriComponentsBuilder.newInstance()
        .path(property.getContextPath() + "/organizations/{organizationId}/clients/membership")
        .queryParam("clientId", clientId)
        .build(organizationId);

    try {
      ClientMembershipModel result = restClient.get()
          .uri(uri)
          .attributes(CLIENT_ATTRIBUTES)
          .retrieve()
          .body(ClientMembershipModel.class);
      return Optional.ofNullable(result).map(ClientMembershipModel::personalDiscount);
    } catch (HttpStatusCodeException e) {
      if (e.getStatusCode().value() == 404) {
        return Optional.empty();
      }
      log.error("Cannot get client discount for org {} client {} -> {}: {}",
          organizationId, clientId, e.getStatusCode(), e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public PageResponse<OrganizationModel> getOrganizations(Pageable pageable) {
    URI uri = UriComponentsBuilder.newInstance()
        .path(property.getContextPath() + "/organizations/all")
        .queryParam("hasOzonIntegration", true)
        .queryParam("page", pageable.getPageNumber())
        .queryParam("size", pageable.getPageSize())
        .build()
        .toUri();

    try {
      PageResponse<OrganizationModel> result = restClient.get()
          .uri(uri)
          .attributes(CLIENT_ATTRIBUTES)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {
          });
      return result != null ? result : new PageResponse<>();
    } catch (HttpStatusCodeException exception) {
      log.error("Cannot get organizations -> status: {}; message: {}",
          exception.getStatusCode(), exception.getMessage());
    }
    return new PageResponse<>();
  }
}
