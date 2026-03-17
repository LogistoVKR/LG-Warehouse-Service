package kz.logisto.lgwarehouseservice.service.impl;

import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {

  private final UserService userService;

  @Override
  public void isMemberOrThrow(String userId, UUID organizationId) throws NotFoundException {
    if (!userService.isMember(userId, organizationId)) {
      throw new NotFoundException();
    }
  }

  @Override
  public void canManageWarehouseOrThrow(String userId, UUID organizationId)
      throws NotFoundException {
    if (!userService.canManageWarehouse(userId, organizationId)) {
      throw new NotFoundException();
    }
  }
}
