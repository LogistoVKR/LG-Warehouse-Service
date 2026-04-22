package kz.logisto.lgwarehouseservice.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantPointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.key.ItemVariantPointOfStorageId;
import kz.logisto.lgwarehouseservice.data.enums.PointOfStorageType;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantPointOfStorageRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.data.repository.PointOfStorageRepository;
import kz.logisto.lgwarehouseservice.integration.BaseIntegrationTest;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

@Transactional
class ItemVariantPointOfStorageRepositoryTest extends BaseIntegrationTest {

  @Autowired
  private ItemVariantPointOfStorageRepository repository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private ItemVariantRepository itemVariantRepository;

  @Autowired
  private PointOfStorageRepository pointOfStorageRepository;

  @Autowired
  private EntityManager entityManager;

  private ItemVariant itemVariant;
  private PointOfStorage warehouse;
  private PointOfStorage pointOfSale;

  @BeforeEach
  void setUp() {
    Item item = new Item();
    item.setName("Test Item");
    item.setDescription("Test Description");
    item.setOrganizationId(UUID.randomUUID());
    item = itemRepository.save(item);

    ItemVariant variant = new ItemVariant();
    variant.setSku("SKU-001");
    variant.setBarcode("BAR-001");
    variant.setItem(item);
    variant.setPrice(BigDecimal.valueOf(100));
    variant.setCurrency(Currency.getInstance("USD"));
    itemVariant = itemVariantRepository.save(variant);

    PointOfStorage wh = new PointOfStorage();
    wh.setName("Warehouse 1");
    wh.setLocation("City A");
    wh.setOrganizationId(item.getOrganizationId());
    wh.setType(PointOfStorageType.WAREHOUSE);
    warehouse = pointOfStorageRepository.save(wh);

    PointOfStorage pos = new PointOfStorage();
    pos.setName("POS 1");
    pos.setLocation("City B");
    pos.setOrganizationId(item.getOrganizationId());
    pos.setType(PointOfStorageType.POINT_OF_SALE);
    pointOfSale = pointOfStorageRepository.save(pos);

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void increment_newRecord_insertsWithQuantity() {
    repository.increment(itemVariant.getId(), warehouse.getId(), 10);
    entityManager.flush();
    entityManager.clear();

    var result = repository.findById(
        new ItemVariantPointOfStorageId(itemVariant.getId(), warehouse.getId()));

    assertTrue(result.isPresent());
    assertEquals(10, result.get().getQuantity());
    assertEquals(0, result.get().getReserved());
  }

  @Test
  void increment_existingRecord_addsToQuantity() {
    repository.increment(itemVariant.getId(), warehouse.getId(), 10);
    entityManager.flush();
    entityManager.clear();

    repository.increment(itemVariant.getId(), warehouse.getId(), 5);
    entityManager.flush();
    entityManager.clear();

    var result = repository.findById(
        new ItemVariantPointOfStorageId(itemVariant.getId(), warehouse.getId()));

    assertTrue(result.isPresent());
    assertEquals(15, result.get().getQuantity());
  }

  @Test
  void decrement_existingRecord_subtractsFromQuantity() {
    repository.increment(itemVariant.getId(), warehouse.getId(), 10);
    entityManager.flush();
    entityManager.clear();

    repository.decrement(itemVariant.getId(), warehouse.getId(), 3);
    entityManager.flush();
    entityManager.clear();

    var result = repository.findById(
        new ItemVariantPointOfStorageId(itemVariant.getId(), warehouse.getId()));

    assertTrue(result.isPresent());
    assertEquals(7, result.get().getQuantity());
  }

  @Test
  void decrement_newRecord_insertsWithNegativeQuantity() {
    repository.decrement(itemVariant.getId(), warehouse.getId(), 5);
    entityManager.flush();
    entityManager.clear();

    var result = repository.findById(
        new ItemVariantPointOfStorageId(itemVariant.getId(), warehouse.getId()));

    assertTrue(result.isPresent());
    assertEquals(-5, result.get().getQuantity());
  }

  @Test
  void reserve_existingRecord_transfersQuantityToReserved() {
    repository.increment(itemVariant.getId(), warehouse.getId(), 10);
    entityManager.flush();
    entityManager.clear();

    repository.reserve(itemVariant.getId(), warehouse.getId(), 3);
    entityManager.flush();
    entityManager.clear();

    var result = repository.findById(
        new ItemVariantPointOfStorageId(itemVariant.getId(), warehouse.getId()));

    assertTrue(result.isPresent());
    assertEquals(7, result.get().getQuantity());
    assertEquals(3, result.get().getReserved());
  }

  @Test
  void release_existingRecord_transfersReservedToQuantity() {
    repository.increment(itemVariant.getId(), warehouse.getId(), 10);
    entityManager.flush();
    entityManager.clear();

    repository.reserve(itemVariant.getId(), warehouse.getId(), 3);
    entityManager.flush();
    entityManager.clear();

    repository.release(itemVariant.getId(), warehouse.getId(), 3);
    entityManager.flush();
    entityManager.clear();

    var result = repository.findById(
        new ItemVariantPointOfStorageId(itemVariant.getId(), warehouse.getId()));

    assertTrue(result.isPresent());
    assertEquals(10, result.get().getQuantity());
    assertEquals(0, result.get().getReserved());
  }

  @Test
  void increment_multiplePointsOfStorage_isolatedUpdates() {
    repository.increment(itemVariant.getId(), warehouse.getId(), 10);
    repository.increment(itemVariant.getId(), pointOfSale.getId(), 5);
    entityManager.flush();
    entityManager.clear();

    var whResult = repository.findById(
        new ItemVariantPointOfStorageId(itemVariant.getId(), warehouse.getId()));
    var posResult = repository.findById(
        new ItemVariantPointOfStorageId(itemVariant.getId(), pointOfSale.getId()));

    assertTrue(whResult.isPresent());
    assertTrue(posResult.isPresent());
    assertEquals(10, whResult.get().getQuantity());
    assertEquals(5, posResult.get().getQuantity());
  }

  @Test
  void findByItemVariantId_returnsAllPointsOfStorage() {
    repository.increment(itemVariant.getId(), warehouse.getId(), 10);
    repository.increment(itemVariant.getId(), pointOfSale.getId(), 5);
    entityManager.flush();
    entityManager.clear();

    List<ItemVariantPointOfStorage> results =
        repository.findByItemVariantId(itemVariant.getId());

    assertNotNull(results);
    assertEquals(2, results.size());
  }

  @Test
  void findByIdItemVariantIdIn_returnsForMultipleVariants() {
    ItemVariant variant2 = new ItemVariant();
    variant2.setSku("SKU-002");
    variant2.setItem(itemVariant.getItem());
    variant2.setPrice(BigDecimal.valueOf(200));
    variant2.setCurrency(Currency.getInstance("USD"));
    variant2 = itemVariantRepository.save(variant2);
    entityManager.flush();
    entityManager.clear();

    repository.increment(itemVariant.getId(), warehouse.getId(), 10);
    repository.increment(variant2.getId(), warehouse.getId(), 20);
    entityManager.flush();
    entityManager.clear();

    List<ItemVariantPointOfStorage> results =
        repository.findByIdItemVariantIdIn(Set.of(itemVariant.getId(), variant2.getId()));

    assertNotNull(results);
    assertEquals(2, results.size());
  }
}
