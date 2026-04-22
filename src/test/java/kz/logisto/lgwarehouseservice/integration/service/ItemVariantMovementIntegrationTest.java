package kz.logisto.lgwarehouseservice.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import kz.logisto.lgwarehouseservice.data.dto.itemmovement.CreateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.UpdateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantPointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.key.ItemVariantPointOfStorageId;
import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import kz.logisto.lgwarehouseservice.data.enums.PointOfStorageType;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantMovementModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantMovementRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantPointOfStorageRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.data.repository.PointOfStorageRepository;
import kz.logisto.lgwarehouseservice.integration.BaseIntegrationTest;
import kz.logisto.lgwarehouseservice.service.ItemVariantMovementService;
import kz.logisto.lgwarehouseservice.util.TestPrincipalFactory;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

class ItemVariantMovementIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private ItemVariantMovementService movementService;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private ItemVariantRepository itemVariantRepository;

  @Autowired
  private PointOfStorageRepository pointOfStorageRepository;

  @Autowired
  private ItemVariantPointOfStorageRepository stockRepository;

  @Autowired
  private ItemVariantMovementRepository movementRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  private final Principal principal = TestPrincipalFactory.create();
  private UUID orgId;
  private UUID itemVariantId;
  private UUID warehouseId;
  private UUID pointOfSaleId;

  @BeforeEach
  void setUp() {
    orgId = UUID.randomUUID();

    transactionTemplate.executeWithoutResult(status -> {
      Item item = new Item();
      item.setName("Integration Item");
      item.setOrganizationId(orgId);
      item = itemRepository.save(item);

      ItemVariant variant = new ItemVariant();
      variant.setSku("INT-SKU-001");
      variant.setItem(item);
      variant.setPrice(BigDecimal.valueOf(50));
      variant.setCurrency(Currency.getInstance("USD"));
      variant = itemVariantRepository.save(variant);
      itemVariantId = variant.getId();

      PointOfStorage wh = new PointOfStorage();
      wh.setName("Integration Warehouse");
      wh.setOrganizationId(orgId);
      wh.setType(PointOfStorageType.WAREHOUSE);
      wh = pointOfStorageRepository.save(wh);
      warehouseId = wh.getId();

      PointOfStorage pos = new PointOfStorage();
      pos.setName("Integration POS");
      pos.setOrganizationId(orgId);
      pos.setType(PointOfStorageType.POINT_OF_SALE);
      pos = pointOfStorageRepository.save(pos);
      pointOfSaleId = pos.getId();

      stockRepository.increment(itemVariantId, warehouseId, 100);
      stockRepository.increment(itemVariantId, pointOfSaleId, 20);
    });
  }

  @AfterEach
  void tearDown() {
    transactionTemplate.executeWithoutResult(status -> {
      movementRepository.deleteAll();
      stockRepository.deleteAll();
      itemVariantRepository.deleteAll();
      pointOfStorageRepository.deleteAll();
      itemRepository.deleteAll();
    });
  }

  @Test
  void createSaleMovement_decrementsStock() {
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        warehouseId, null, itemVariantId,
        BigDecimal.valueOf(50), Currency.getInstance("USD"),
        10, "Test sale", MovementType.SALE, LocalDateTime.now(), orgId);

    ItemVariantMovementModel result = movementService.create(dto, principal);

    assertNotNull(result);
    assertNotNull(result.getId());

    ItemVariantPointOfStorage stock = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, warehouseId)).orElseThrow();
    assertEquals(90, stock.getQuantity());
  }

  @Test
  void createPurchaseMovement_incrementsStock() {
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        null, warehouseId, itemVariantId,
        BigDecimal.valueOf(30), Currency.getInstance("USD"),
        25, "Restock", MovementType.PURCHASE, LocalDateTime.now(), orgId);

    ItemVariantMovementModel result = movementService.create(dto, principal);

    assertNotNull(result);

    ItemVariantPointOfStorage stock = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, warehouseId)).orElseThrow();
    assertEquals(125, stock.getQuantity());
  }

  @Test
  void createTransferMovement_movesStockBetweenLocations() {
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        warehouseId, pointOfSaleId, itemVariantId,
        null, null,
        15, "Transfer to POS", MovementType.TRANSFER, LocalDateTime.now(), orgId);

    ItemVariantMovementModel result = movementService.create(dto, principal);

    assertNotNull(result);

    ItemVariantPointOfStorage whStock = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, warehouseId)).orElseThrow();
    ItemVariantPointOfStorage posStock = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, pointOfSaleId)).orElseThrow();

    assertEquals(85, whStock.getQuantity());
    assertEquals(35, posStock.getQuantity());
  }

  @Test
  void createReserveMovement_reservesStock() {
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        warehouseId, null, itemVariantId,
        null, null,
        8, "Reserve for order", MovementType.RESERVE, LocalDateTime.now(), orgId);

    ItemVariantMovementModel result = movementService.create(dto, principal);

    assertNotNull(result);

    ItemVariantPointOfStorage stock = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, warehouseId)).orElseThrow();
    assertEquals(92, stock.getQuantity());
    assertEquals(8, stock.getReserved());
  }

  @Test
  void deleteMovement_undoesStockChanges() {
    CreateItemVariantMovementDto dto = new CreateItemVariantMovementDto(
        warehouseId, null, itemVariantId,
        BigDecimal.valueOf(50), Currency.getInstance("USD"),
        10, "Sale to undo", MovementType.SALE, LocalDateTime.now(), orgId);

    ItemVariantMovementModel created = movementService.create(dto, principal);

    ItemVariantPointOfStorage afterSale = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, warehouseId)).orElseThrow();
    assertEquals(90, afterSale.getQuantity());

    movementService.delete(created.getId(), principal);

    ItemVariantPointOfStorage afterDelete = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, warehouseId)).orElseThrow();
    assertEquals(100, afterDelete.getQuantity());
  }

  @Test
  void updateMovement_reappliesStock() {
    CreateItemVariantMovementDto createDto = new CreateItemVariantMovementDto(
        warehouseId, null, itemVariantId,
        BigDecimal.valueOf(50), Currency.getInstance("USD"),
        10, "Initial sale", MovementType.SALE, LocalDateTime.now(), orgId);

    ItemVariantMovementModel created = movementService.create(createDto, principal);

    ItemVariantPointOfStorage afterCreate = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, warehouseId)).orElseThrow();
    assertEquals(90, afterCreate.getQuantity());

    UpdateItemVariantMovementDto updateDto = new UpdateItemVariantMovementDto(
        warehouseId, null, itemVariantId,
        BigDecimal.valueOf(50), Currency.getInstance("USD"),
        20, "Updated sale", MovementType.SALE, LocalDateTime.now());

    movementService.update(created.getId(), updateDto, principal);

    ItemVariantPointOfStorage afterUpdate = stockRepository.findById(
        new ItemVariantPointOfStorageId(itemVariantId, warehouseId)).orElseThrow();
    assertEquals(80, afterUpdate.getQuantity());
  }
}
