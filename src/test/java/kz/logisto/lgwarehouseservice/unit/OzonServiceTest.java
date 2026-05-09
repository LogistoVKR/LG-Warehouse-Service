package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.data.model.OzonApiKeyModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.mapper.ItemMapper;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.OzonService;
import kz.logisto.lgwarehouseservice.service.UserService;
import kz.logisto.lgwarehouseservice.service.impl.OzonServiceImpl;
import kz.logisto.lgwarehouseservice.util.TestPrincipalFactory;
import java.security.Principal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class OzonServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private AccessService accessService;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private ItemVariantRepository itemVariantRepository;

  @Mock
  private ItemMapper itemMapper;

  @Mock
  private ItemVariantMapper itemVariantMapper;

  @Mock
  private RestClient ozonRestClient;

  @Mock
  private TransactionTemplate transactionTemplate;

  private OzonService service;

  @BeforeEach
  void init() {
    service = new OzonServiceImpl(itemMapper, userService, ozonRestClient, accessService,
        itemRepository, itemVariantMapper, itemVariantRepository, transactionTemplate);
  }

  @Test
  void syncProducts_nullApiKey_doesNothing() {
    UUID orgId = UUID.randomUUID();
    Principal principal = TestPrincipalFactory.create();
    doNothing().when(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    when(userService.getOzonApiKeyByOrganizationId(orgId)).thenReturn(null);

    service.syncProducts(orgId, principal);

    verify(itemRepository, never()).save(any());
    verify(itemVariantRepository, never()).save(any());
  }

  @Test
  void syncProducts_noIntegration_doesNothing() {
    UUID orgId = UUID.randomUUID();
    Principal principal = TestPrincipalFactory.create();
    OzonApiKeyModel model = new OzonApiKeyModel("key", "clientId", false);
    doNothing().when(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    when(userService.getOzonApiKeyByOrganizationId(orgId)).thenReturn(model);

    service.syncProducts(orgId, principal);

    verify(itemRepository, never()).save(any());
    verify(itemVariantRepository, never()).save(any());
  }

  @Test
  void truncate_shortString_returnsUnchanged() {
    String value = "short";
    assertEquals(value, OzonServiceImpl.truncate(value, 255));
  }

  @Test
  void truncate_longString_isCutAt255() {
    String value = "a".repeat(300);
    String result = OzonServiceImpl.truncate(value, 255);
    assertEquals(255, result.length());
  }

  @Test
  void truncate_null_returnsNull() {
    assertNull(OzonServiceImpl.truncate(null, 255));
  }

  @Test
  void truncate_exactLength_returnsUnchanged() {
    String value = "a".repeat(255);
    assertEquals(value, OzonServiceImpl.truncate(value, 255));
  }
}
