package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.data.model.CountItemVariantPointOfStorageModel;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import kz.logisto.lgwarehouseservice.service.OrganizationService;
import kz.logisto.lgwarehouseservice.service.PointOfStorageService;
import kz.logisto.lgwarehouseservice.service.impl.OrganizationServiceImpl;
import kz.logisto.lgwarehouseservice.util.TestPrincipalFactory;
import java.security.Principal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private AccessService accessService;

  @Mock
  private ItemVariantService itemVariantService;

  @Mock
  private PointOfStorageService pointOfStorageService;

  private OrganizationService service;

  @BeforeEach
  void init() {
    service = new OrganizationServiceImpl(accessService, itemVariantService, pointOfStorageService);
  }

  @Test
  void countItemVariantsAndPointsOfStorage_valid_returnsCounts() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    when(itemVariantService.countByOrganizationId(orgId)).thenReturn(15);
    when(pointOfStorageService.countByOrganizationId(orgId)).thenReturn(3);

    CountItemVariantPointOfStorageModel result =
        service.countItemVariantsAndPointsOfStorage(orgId, principal);

    assertEquals(15, result.getItemVariants());
    assertEquals(3, result.getPointsOfStorage());
  }

  @Test
  void countItemVariantsAndPointsOfStorage_accessDenied_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    doThrow(new NotFoundException()).when(accessService)
        .isMemberOrThrow(principal.getName(), orgId);

    assertThrows(NotFoundException.class,
        () -> service.countItemVariantsAndPointsOfStorage(orgId, principal));
  }
}
