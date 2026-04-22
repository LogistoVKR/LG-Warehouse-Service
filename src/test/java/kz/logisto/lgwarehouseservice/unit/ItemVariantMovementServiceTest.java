package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.data.dto.itemmovement.CreateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.ItemVariantMovementFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.UpdateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.dto.itemvariantpointofstorage.ItemVariantPointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantMovement;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantMovementModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantMovementRepository;
import kz.logisto.lgwarehouseservice.exception.NotEqualValuesException;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantMovementMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemVariantMovementService;
import kz.logisto.lgwarehouseservice.service.ItemVariantPointOfStorageService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import kz.logisto.lgwarehouseservice.service.PointOfStorageService;
import kz.logisto.lgwarehouseservice.service.impl.ItemVariantMovementServiceImpl;
import kz.logisto.lgwarehouseservice.util.TestPrincipalFactory;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
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
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ItemVariantMovementServiceTest {

  @Mock
  private AccessService accessService;

  @Mock
  private ItemVariantMovementMapper mapper;

  @Mock
  private ItemVariantService itemVariantService;

  @Mock
  private ItemVariantMovementRepository repository;

  @Mock
  private PointOfStorageService pointOfStorageService;

  @Mock
  private ItemVariantPointOfStorageService itemVariantPointOfStorageService;

  private ItemVariantMovementService service;

  private final UUID orgId = UUID.randomUUID();
  private final UUID fromPosId = UUID.randomUUID();
  private final UUID toPosId = UUID.randomUUID();
  private final UUID itemVariantId = UUID.randomUUID();

  @BeforeEach
  void init() {
    service = new ItemVariantMovementServiceImpl(accessService, mapper, itemVariantService,
        repository, pointOfStorageService, itemVariantPointOfStorageService);
  }

  @Test
  void create_validSale_savesAndUpdatesStock() {
    Principal principal = TestPrincipalFactory.create();
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        fromPosId, null, itemVariantId, BigDecimal.TEN, Currency.getInstance("USD"),
        5, "sale reason", MovementType.SALE, LocalDateTime.now(), orgId);

    ItemVariantMovement movement = new ItemVariantMovement();
    movement.setType(MovementType.SALE);
    movement.setQuantity(5);

    PointOfStorage fromPos = new PointOfStorage();
    fromPos.setId(fromPosId);
    fromPos.setOrganizationId(orgId);

    Item parentItem = new Item();
    parentItem.setOrganizationId(orgId);
    ItemVariant variant = new ItemVariant();
    variant.setId(itemVariantId);
    variant.setItem(parentItem);

    ItemVariantMovement saved = new ItemVariantMovement();
    saved.setId(UUID.randomUUID());
    ItemVariantMovementModel model = new ItemVariantMovementModel();

    when(mapper.toEntity(dto)).thenReturn(movement);
    when(pointOfStorageService.getOrThrow(fromPosId)).thenReturn(fromPos);
    when(itemVariantService.getOrThrow(itemVariantId)).thenReturn(variant);
    when(repository.save(movement)).thenReturn(saved);
    when(mapper.toModel(saved)).thenReturn(model);

    ItemVariantMovementModel result = service.create(dto, principal);

    assertNotNull(result);
    verify(accessService, atLeastOnce()).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(itemVariantPointOfStorageService).update(any(ItemVariantPointOfStorageDto.class));
    verify(repository).save(movement);
  }

  @Test
  void create_accessDenied_throws() {
    Principal principal = TestPrincipalFactory.create();
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        fromPosId, null, itemVariantId, null, null, 5, null, MovementType.SALE, null, orgId);
    ItemVariantMovement movement = new ItemVariantMovement();

    when(mapper.toEntity(dto)).thenReturn(movement);
    doThrow(new NotFoundException()).when(accessService)
        .canManageWarehouseOrThrow(principal.getName(), orgId);

    assertThrows(NotFoundException.class, () -> service.create(dto, principal));
  }

  @Test
  void create_fromPosNotFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        fromPosId, null, itemVariantId, null, null, 5, null, MovementType.SALE, null, orgId);
    ItemVariantMovement movement = new ItemVariantMovement();
    movement.setType(MovementType.SALE);

    when(mapper.toEntity(dto)).thenReturn(movement);
    when(pointOfStorageService.getOrThrow(fromPosId)).thenThrow(new NotFoundException());

    assertThrows(NotFoundException.class, () -> service.create(dto, principal));
  }

  @Test
  void create_itemVariantNotFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        fromPosId, null, itemVariantId, null, null, 5, null, MovementType.SALE, null, orgId);
    ItemVariantMovement movement = new ItemVariantMovement();
    movement.setType(MovementType.SALE);

    PointOfStorage fromPos = new PointOfStorage();
    fromPos.setId(fromPosId);
    fromPos.setOrganizationId(orgId);

    when(mapper.toEntity(dto)).thenReturn(movement);
    when(pointOfStorageService.getOrThrow(fromPosId)).thenReturn(fromPos);
    when(itemVariantService.getOrThrow(itemVariantId)).thenThrow(new NotFoundException());

    assertThrows(NotFoundException.class, () -> service.create(dto, principal));
  }

  @Test
  void create_differentOrgs_throwsNotEqualValues() {
    Principal principal = TestPrincipalFactory.create();
    UUID otherOrgId = UUID.randomUUID();
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        fromPosId, null, itemVariantId, null, null, 5, null, MovementType.SALE, null, orgId);
    ItemVariantMovement movement = new ItemVariantMovement();
    movement.setType(MovementType.SALE);

    PointOfStorage fromPos = new PointOfStorage();
    fromPos.setId(fromPosId);
    fromPos.setOrganizationId(orgId);

    Item parentItem = new Item();
    parentItem.setOrganizationId(otherOrgId);
    ItemVariant variant = new ItemVariant();
    variant.setId(itemVariantId);
    variant.setItem(parentItem);

    when(mapper.toEntity(dto)).thenReturn(movement);
    when(pointOfStorageService.getOrThrow(fromPosId)).thenReturn(fromPos);
    when(itemVariantService.getOrThrow(itemVariantId)).thenReturn(variant);

    assertThrows(NotEqualValuesException.class, () -> service.create(dto, principal));
  }

  @Test
  void update_valid_undoesThenReapplies() {
    Principal principal = TestPrincipalFactory.create();
    UUID movementId = UUID.randomUUID();
    UpdateItemVariantMovementDto dto = new UpdateItemVariantMovementDto(
        fromPosId, null, itemVariantId, null, null, 10, null, null, null);

    PointOfStorage fromPos = new PointOfStorage();
    fromPos.setId(fromPosId);
    fromPos.setOrganizationId(orgId);

    Item parentItem = new Item();
    parentItem.setOrganizationId(orgId);
    ItemVariant variant = new ItemVariant();
    variant.setId(itemVariantId);
    variant.setItem(parentItem);

    ItemVariantMovement existing = new ItemVariantMovement();
    existing.setId(movementId);
    existing.setType(MovementType.SALE);
    existing.setQuantity(5);
    existing.setOrganizationId(orgId);
    existing.setFromPointOfStorage(fromPos);
    existing.setItemVariant(variant);

    ItemVariantMovement saved = new ItemVariantMovement();
    saved.setId(movementId);
    ItemVariantMovementModel model = new ItemVariantMovementModel();

    when(repository.findById(movementId)).thenReturn(Optional.of(existing));
    when(pointOfStorageService.getOrThrow(fromPosId)).thenReturn(fromPos);
    when(itemVariantService.getOrThrow(itemVariantId)).thenReturn(variant);
    when(repository.save(existing)).thenReturn(saved);
    when(mapper.toModel(saved)).thenReturn(model);

    ItemVariantMovementModel result = service.update(movementId, dto, principal);

    assertNotNull(result);
    verify(itemVariantPointOfStorageService).undoUpdate(any(ItemVariantPointOfStorageDto.class));
    verify(mapper).updateEntity(existing, dto);
    verify(itemVariantPointOfStorageService).update(any(ItemVariantPointOfStorageDto.class));
  }

  @Test
  void update_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID movementId = UUID.randomUUID();
    UpdateItemVariantMovementDto dto = new UpdateItemVariantMovementDto(
        null, null, null, null, null, 10, null, null, null);
    when(repository.findById(movementId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.update(movementId, dto, principal));
  }

  @Test
  void update_accessDenied_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID movementId = UUID.randomUUID();
    UpdateItemVariantMovementDto dto = new UpdateItemVariantMovementDto(
        null, null, null, null, null, 10, null, null, null);

    ItemVariantMovement existing = new ItemVariantMovement();
    existing.setId(movementId);
    existing.setOrganizationId(orgId);

    when(repository.findById(movementId)).thenReturn(Optional.of(existing));
    doThrow(new NotFoundException()).when(accessService)
        .canManageWarehouseOrThrow(principal.getName(), orgId);

    assertThrows(NotFoundException.class, () -> service.update(movementId, dto, principal));
  }

  @Test
  void delete_valid_undoesStockAndDeletes() {
    Principal principal = TestPrincipalFactory.create();
    UUID movementId = UUID.randomUUID();

    PointOfStorage fromPos = new PointOfStorage();
    fromPos.setId(fromPosId);

    ItemVariant variant = new ItemVariant();
    variant.setId(itemVariantId);

    ItemVariantMovement movement = new ItemVariantMovement();
    movement.setId(movementId);
    movement.setType(MovementType.SALE);
    movement.setQuantity(5);
    movement.setOrganizationId(orgId);
    movement.setFromPointOfStorage(fromPos);
    movement.setItemVariant(variant);

    when(repository.findById(movementId)).thenReturn(Optional.of(movement));

    service.delete(movementId, principal);

    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(itemVariantPointOfStorageService).undoUpdate(any(ItemVariantPointOfStorageDto.class));
    verify(repository).delete(movement);
  }

  @Test
  void delete_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID movementId = UUID.randomUUID();
    when(repository.findById(movementId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.delete(movementId, principal));
  }

  @Test
  void getOrThrow_found_returnsEntity() {
    UUID id = UUID.randomUUID();
    ItemVariantMovement movement = new ItemVariantMovement();
    movement.setId(id);
    when(repository.findById(id)).thenReturn(Optional.of(movement));

    ItemVariantMovement result = service.getOrThrow(id);

    assertNotNull(result);
  }

  @Test
  void getOrThrow_notFound_throws() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getOrThrow(id));
  }

  @Test
  @SuppressWarnings("unchecked")
  void getAllPageable_valid_returnsPage() {
    Principal principal = TestPrincipalFactory.create();
    Pageable pageable = PageRequest.of(0, 10);
    ItemVariantMovementFilterDto filter = new ItemVariantMovementFilterDto(
        null, null, null, null, null, null);
    ItemVariantMovement movement = new ItemVariantMovement();
    Page<ItemVariantMovement> page = new PageImpl<>(List.of(movement));
    ItemVariantMovementModel model = new ItemVariantMovementModel();

    when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(mapper.toModel(movement)).thenReturn(model);

    Page<ItemVariantMovementModel> result =
        service.getAllPageable(orgId, filter, pageable, principal);

    assertNotNull(result);
    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
  }
}
