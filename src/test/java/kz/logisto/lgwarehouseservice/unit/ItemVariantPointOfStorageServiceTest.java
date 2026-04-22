package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.data.dto.itemvariantpointofstorage.ItemVariantPointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantPointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.key.ItemVariantPointOfStorageId;
import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantPointOfStorageModel;
import kz.logisto.lgwarehouseservice.data.model.PointOfStorageModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantPointOfStorageRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.exception.UpdateItemVariantPointOfStorageException;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantPointOfStorageMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemVariantPointOfStorageService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import kz.logisto.lgwarehouseservice.service.impl.ItemVariantPointOfStorageServiceImpl;
import kz.logisto.lgwarehouseservice.util.TestPrincipalFactory;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemVariantPointOfStorageServiceTest {

  @Mock
  private AccessService accessService;

  @Mock
  private ItemVariantService itemVariantService;

  @Mock
  private ItemVariantPointOfStorageMapper mapper;

  @Mock
  private ItemVariantPointOfStorageRepository repository;

  private ItemVariantPointOfStorageService service;

  private final UUID itemVariantId = UUID.randomUUID();
  private final UUID fromPosId = UUID.randomUUID();
  private final UUID toPosId = UUID.randomUUID();

  @BeforeEach
  void init() {
    service = new ItemVariantPointOfStorageServiceImpl(accessService, itemVariantService, mapper,
        repository);
  }

  // ===== update: SALE =====

  @Test
  void update_sale_valid_decrementsFrom() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.SALE, fromPosId, null, itemVariantId, 5);
    service.update(dto);
    verify(repository).decrement(itemVariantId, fromPosId, 5);
  }

  @Test
  void update_sale_nullItemVariantId_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.SALE, fromPosId, null, null, 5);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  @Test
  void update_sale_nullFromPos_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.SALE, null, null, itemVariantId, 5);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  @Test
  void update_sale_zeroQuantity_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.SALE, fromPosId, null, itemVariantId, 0);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  @Test
  void update_sale_negativeQuantity_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.SALE, fromPosId, null, itemVariantId,
        -1);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  // ===== update: RETURN =====

  @Test
  void update_return_valid_incrementsTo() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.RETURN, null, toPosId, itemVariantId,
        3);
    service.update(dto);
    verify(repository).increment(itemVariantId, toPosId, 3);
  }

  @Test
  void update_return_nullToPos_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.RETURN, null, null, itemVariantId, 3);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  // ===== update: RESERVE =====

  @Test
  void update_reserve_valid_reservesFrom() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.RESERVE, fromPosId, null, itemVariantId,
        2);
    service.update(dto);
    verify(repository).reserve(itemVariantId, fromPosId, 2);
  }

  @Test
  void update_reserve_nullFromPos_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.RESERVE, null, null, itemVariantId, 2);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  // ===== update: PURCHASE =====

  @Test
  void update_purchase_valid_incrementsTo() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.PURCHASE, null, toPosId, itemVariantId,
        10);
    service.update(dto);
    verify(repository).increment(itemVariantId, toPosId, 10);
  }

  @Test
  void update_purchase_nullToPos_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.PURCHASE, null, null, itemVariantId,
        10);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  // ===== update: TRANSFER =====

  @Test
  void update_transfer_valid_decrementsFromIncrementsTo() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.TRANSFER, fromPosId, toPosId,
        itemVariantId, 4);
    service.update(dto);
    verify(repository).decrement(itemVariantId, fromPosId, 4);
    verify(repository).increment(itemVariantId, toPosId, 4);
  }

  @Test
  void update_transfer_nullFromPos_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.TRANSFER, null, toPosId, itemVariantId,
        4);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  @Test
  void update_transfer_nullToPos_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.TRANSFER, fromPosId, null,
        itemVariantId, 4);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  // ===== update: WRITE_OFF =====

  @Test
  void update_writeOff_valid_decrementsFrom() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.WRITE_OFF, fromPosId, null,
        itemVariantId, 1);
    service.update(dto);
    verify(repository).decrement(itemVariantId, fromPosId, 1);
  }

  @Test
  void update_writeOff_nullFromPos_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.WRITE_OFF, null, null, itemVariantId,
        1);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.update(dto));
  }

  // ===== undoUpdate: SALE =====

  @Test
  void undoUpdate_sale_incrementsFrom() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.SALE, fromPosId, null, itemVariantId,
        5);
    service.undoUpdate(dto);
    verify(repository).increment(itemVariantId, fromPosId, 5);
  }

  @Test
  void undoUpdate_sale_nullItemVariantId_throws() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.SALE, fromPosId, null, null, 5);
    assertThrows(UpdateItemVariantPointOfStorageException.class, () -> service.undoUpdate(dto));
  }

  // ===== undoUpdate: RETURN =====

  @Test
  void undoUpdate_return_decrementsTo() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.RETURN, null, toPosId, itemVariantId,
        3);
    service.undoUpdate(dto);
    verify(repository).decrement(itemVariantId, toPosId, 3);
  }

  // ===== undoUpdate: RESERVE =====

  @Test
  void undoUpdate_reserve_releasesFrom() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.RESERVE, fromPosId, null, itemVariantId,
        2);
    service.undoUpdate(dto);
    verify(repository).release(itemVariantId, fromPosId, 2);
  }

  // ===== undoUpdate: PURCHASE =====

  @Test
  void undoUpdate_purchase_decrementsTo() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.PURCHASE, null, toPosId, itemVariantId,
        10);
    service.undoUpdate(dto);
    verify(repository).decrement(itemVariantId, toPosId, 10);
  }

  // ===== undoUpdate: TRANSFER =====

  @Test
  void undoUpdate_transfer_reversesDirection() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.TRANSFER, fromPosId, toPosId,
        itemVariantId, 4);
    service.undoUpdate(dto);
    verify(repository).decrement(itemVariantId, toPosId, 4);
    verify(repository).increment(itemVariantId, fromPosId, 4);
  }

  // ===== undoUpdate: WRITE_OFF =====

  @Test
  void undoUpdate_writeOff_incrementsFrom() {
    var dto = new ItemVariantPointOfStorageDto(MovementType.WRITE_OFF, fromPosId, null,
        itemVariantId, 1);
    service.undoUpdate(dto);
    verify(repository).increment(itemVariantId, fromPosId, 1);
  }

  // ===== getCountsByItemVariantId =====

  @Test
  void getCountsByItemVariantId_valid_returnsList() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    Item parentItem = new Item();
    parentItem.setOrganizationId(orgId);
    ItemVariant variant = new ItemVariant();
    variant.setId(itemVariantId);
    variant.setItem(parentItem);

    PointOfStorage pos = new PointOfStorage();
    pos.setId(fromPosId);
    ItemVariantPointOfStorage stock = new ItemVariantPointOfStorage();
    stock.setId(new ItemVariantPointOfStorageId(itemVariantId, fromPosId));
    stock.setQuantity(10);
    stock.setReserved(2);
    stock.setPointOfStorage(pos);

    ItemVariantPointOfStorageModel model = new ItemVariantPointOfStorageModel(10, 2,
        new PointOfStorageModel());

    when(itemVariantService.getOrThrow(itemVariantId)).thenReturn(variant);
    when(repository.findByItemVariantId(itemVariantId)).thenReturn(List.of(stock));
    when(mapper.toModel(stock)).thenReturn(model);

    List<ItemVariantPointOfStorageModel> result =
        service.getCountsByItemVariantId(itemVariantId, principal);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(10, result.getFirst().getQuantity());
    verify(accessService).isMemberOrThrow(principal.getName(), orgId);
  }

  @Test
  void getCountsByItemVariantId_variantNotFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    when(itemVariantService.getOrThrow(itemVariantId)).thenThrow(new NotFoundException());

    assertThrows(NotFoundException.class,
        () -> service.getCountsByItemVariantId(itemVariantId, principal));
  }
}
