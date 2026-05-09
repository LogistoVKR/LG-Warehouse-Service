package kz.logisto.lgwarehouseservice.service;

import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.model.OzonApiKeyModel;

public interface UserService {

  boolean isMember(String userId, UUID organizationId);

  boolean canManageWarehouse(String userId, UUID organizationId);

  OzonApiKeyModel getOzonApiKeyByOrganizationId(UUID organizationId);
}
