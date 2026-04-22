package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.UserService;
import kz.logisto.lgwarehouseservice.service.impl.AccessServiceImpl;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

  @Mock
  private UserService userService;

  private AccessService accessService;

  @BeforeEach
  void init() {
    accessService = new AccessServiceImpl(userService);
  }

  @Test
  void isMemberOrThrow_member_noException() {
    String userId = "user-1";
    UUID orgId = UUID.randomUUID();
    when(userService.isMember(userId, orgId)).thenReturn(true);

    assertDoesNotThrow(() -> accessService.isMemberOrThrow(userId, orgId));
  }

  @Test
  void isMemberOrThrow_notMember_throwsNotFoundException() {
    String userId = "user-1";
    UUID orgId = UUID.randomUUID();
    when(userService.isMember(userId, orgId)).thenReturn(false);

    assertThrows(NotFoundException.class,
        () -> accessService.isMemberOrThrow(userId, orgId));
  }

  @Test
  void canManageWarehouseOrThrow_canManage_noException() {
    String userId = "user-1";
    UUID orgId = UUID.randomUUID();
    when(userService.canManageWarehouse(userId, orgId)).thenReturn(true);

    assertDoesNotThrow(() -> accessService.canManageWarehouseOrThrow(userId, orgId));
  }

  @Test
  void canManageWarehouseOrThrow_cannotManage_throwsNotFoundException() {
    String userId = "user-1";
    UUID orgId = UUID.randomUUID();
    when(userService.canManageWarehouse(userId, orgId)).thenReturn(false);

    assertThrows(NotFoundException.class,
        () -> accessService.canManageWarehouseOrThrow(userId, orgId));
  }
}
