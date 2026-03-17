package kz.logisto.lgwarehouseservice.service;

import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import java.util.UUID;

public interface AccessService {

  void isMemberOrThrow(String userId, UUID organizationId) throws NotFoundException;

  void canManageWarehouseOrThrow(String userId, UUID organizationId) throws NotFoundException;
}

