package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.data.dto.itemvariant.CreateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.dto.itemvariant.UpdateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import kz.logisto.lgwarehouseservice.service.impl.ItemVariantServiceImpl;
import kz.logisto.lgwarehouseservice.util.TestPrincipalFactory;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ItemVariantServiceTest {

  @Mock
  private ItemService itemService;

  @Mock
  private AccessService accessService;

  @Mock
  private ItemVariantMapper itemVariantMapper;

  @Mock
  private ItemVariantRepository itemVariantRepository;

  private ItemVariantService service;

  @BeforeEach
  void init() {
    service = new ItemVariantServiceImpl(itemService, accessService, itemVariantMapper,
        itemVariantRepository);
  }

  @Test
  void findByItemId_valid_returnsPage() {
    Principal principal = TestPrincipalFactory.create();
    UUID itemId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);
    Item item = new Item();
    item.setId(itemId);
    item.setOrganizationId(orgId);
    ItemVariant variant = new ItemVariant();
    Page<ItemVariant> page = new PageImpl<>(List.of(variant));
    ItemVariantModel model = new ItemVariantModel();

    when(itemService.getOrThrow(itemId)).thenReturn(item);
    when(itemVariantRepository.findAllByItemId(itemId, pageable)).thenReturn(page);
    when(itemVariantMapper.toModel(variant)).thenReturn(model);

    Page<ItemVariantModel> result = service.findByItemId(itemId, pageable, principal);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(accessService).isMemberOrThrow(principal.getName(), orgId);
  }

  @Test
  void findByItemId_itemNotFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID itemId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);
    when(itemService.getOrThrow(itemId)).thenThrow(new NotFoundException());

    assertThrows(NotFoundException.class,
        () -> service.findByItemId(itemId, pageable, principal));
  }

  @Test
  void create_valid_savesAndReturns() {
    Principal principal = TestPrincipalFactory.create();
    UUID itemId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    CreateItemVariantDto dto = new CreateItemVariantDto("SKU-001", "BAR-001", itemId,
        BigDecimal.valueOf(100), Currency.getInstance("USD"));
    Item item = new Item();
    item.setId(itemId);
    item.setOrganizationId(orgId);
    ItemVariant variant = new ItemVariant();
    ItemVariant saved = new ItemVariant();
    saved.setId(UUID.randomUUID());
    ItemVariantModel model = new ItemVariantModel();

    when(itemService.getOrThrow(itemId)).thenReturn(item);
    when(itemVariantMapper.toEntity(dto)).thenReturn(variant);
    when(itemVariantRepository.save(variant)).thenReturn(saved);
    when(itemVariantMapper.toModel(saved)).thenReturn(model);

    ItemVariantModel result = service.create(dto, principal);

    assertNotNull(result);
    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(itemVariantRepository).save(variant);
  }

  @Test
  void create_accessDenied_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID itemId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    CreateItemVariantDto dto = new CreateItemVariantDto("SKU-001", null, itemId,
        BigDecimal.valueOf(100), Currency.getInstance("USD"));
    Item item = new Item();
    item.setId(itemId);
    item.setOrganizationId(orgId);

    when(itemService.getOrThrow(itemId)).thenReturn(item);
    doThrow(new NotFoundException()).when(accessService)
        .canManageWarehouseOrThrow(principal.getName(), orgId);

    assertThrows(NotFoundException.class, () -> service.create(dto, principal));
  }

  @Test
  void update_valid_updatesAndReturns() {
    Principal principal = TestPrincipalFactory.create();
    UUID variantId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UpdateItemVariantDto dto = new UpdateItemVariantDto("SKU-002", null, null, null);
    Item item = new Item();
    item.setOrganizationId(orgId);
    ItemVariant variant = new ItemVariant();
    variant.setId(variantId);
    variant.setItem(item);
    ItemVariantModel model = new ItemVariantModel();

    when(itemVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
    when(itemVariantRepository.save(variant)).thenReturn(variant);
    when(itemVariantMapper.toModel(variant)).thenReturn(model);

    ItemVariantModel result = service.update(variantId, dto, principal);

    assertNotNull(result);
    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(itemVariantMapper).updateEntity(variant, dto);
  }

  @Test
  void update_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    UpdateItemVariantDto dto = new UpdateItemVariantDto("SKU", null, null, null);
    when(itemVariantRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.update(id, dto, principal));
  }

  @Test
  void delete_valid_deletes() {
    Principal principal = TestPrincipalFactory.create();
    UUID variantId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    Item item = new Item();
    item.setOrganizationId(orgId);
    ItemVariant variant = new ItemVariant();
    variant.setId(variantId);
    variant.setItem(item);

    when(itemVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));

    service.delete(variantId, principal);

    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(itemVariantRepository).delete(variant);
  }

  @Test
  void delete_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    when(itemVariantRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.delete(id, principal));
  }

  @Test
  void getOrThrow_found_returns() {
    UUID id = UUID.randomUUID();
    ItemVariant variant = new ItemVariant();
    variant.setId(id);
    when(itemVariantRepository.findById(id)).thenReturn(Optional.of(variant));

    ItemVariant result = service.getOrThrow(id);

    assertEquals(id, result.getId());
  }

  @Test
  void getOrThrow_notFound_throws() {
    UUID id = UUID.randomUUID();
    when(itemVariantRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getOrThrow(id));
  }

  @Test
  void countByOrganizationId_returnsCount() {
    UUID orgId = UUID.randomUUID();
    when(itemVariantRepository.countByItem_OrganizationId(orgId)).thenReturn(10);

    int result = service.countByOrganizationId(orgId);

    assertEquals(10, result);
  }
}
