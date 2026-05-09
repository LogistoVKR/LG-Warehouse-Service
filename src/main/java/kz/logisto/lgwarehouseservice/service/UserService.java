package kz.logisto.lgwarehouseservice.service;

import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.dto.PageResponse;
import kz.logisto.lgwarehouseservice.data.model.OrganizationModel;
import kz.logisto.lgwarehouseservice.data.model.OzonApiKeyModel;
import org.springframework.data.domain.Pageable;

public interface UserService {

  boolean isMember(String userId, UUID organizationId);

  boolean canManageWarehouse(String userId, UUID organizationId);

  OzonApiKeyModel getOzonApiKeyByOrganizationId(UUID organizationId);

  PageResponse<OrganizationModel> getOrganizations(Pageable pageable);
}
