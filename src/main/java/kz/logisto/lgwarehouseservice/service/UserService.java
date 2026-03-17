package kz.logisto.lgwarehouseservice.service;

import java.util.UUID;

public interface UserService {

  boolean isMember(String userId, UUID organizationId);

  boolean canManageWarehouse(String userId, UUID organizationId);
}
