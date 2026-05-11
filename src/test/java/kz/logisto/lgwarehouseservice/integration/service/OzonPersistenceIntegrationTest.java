package kz.logisto.lgwarehouseservice.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductInfoResponse;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.integration.BaseIntegrationTest;
import kz.logisto.lgwarehouseservice.service.impl.OzonServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class OzonPersistenceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private OzonServiceImpl ozonService;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private ItemVariantRepository itemVariantRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  void saveItemWithVariants_existingItemAndVariant_updatesWithoutMergeConflict()
      throws ReflectiveOperationException {
    UUID organizationId = UUID.randomUUID();
    Long modelId = 1560002279L;
    Long productId = 1001L;

    Item item = new Item();
    item.setName("Old name");
    item.setDescription("Old description");
    item.setOrganizationId(organizationId);
    item.setOzonModelId(modelId);
    item = itemRepository.save(item);

    ItemVariant variant = new ItemVariant();
    variant.setSku("1001");
    variant.setBarcode("OLD-BARCODE");
    variant.setOzonProductId(productId);
    variant.setItem(item);
    variant.setPrice(BigDecimal.ONE);
    variant.setCurrency(Currency.getInstance("USD"));
    variant = itemVariantRepository.save(variant);

    entityManager.flush();
    entityManager.clear();

    invokeSaveItemWithVariants(organizationId, modelId,
        List.of(ozonProduct(productId, modelId, "New name", 2001L, "NEW-BARCODE", "12.34")),
        Map.of(productId, "New description"));

    entityManager.flush();
    entityManager.clear();

    Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
    ItemVariant updatedVariant = itemVariantRepository.findById(variant.getId()).orElseThrow();

    assertEquals("New name", updatedItem.getName());
    assertEquals("New description", updatedItem.getDescription());
    assertEquals("2001", updatedVariant.getSku());
    assertEquals("NEW-BARCODE", updatedVariant.getBarcode());
    assertEquals(0, BigDecimal.valueOf(12.34).compareTo(updatedVariant.getPrice()));
    assertEquals(item.getId(), updatedVariant.getItem().getId());
  }

  @Test
  void saveItemWithVariants_existingAndNewVariants_persistsBoth()
      throws ReflectiveOperationException {
    UUID organizationId = UUID.randomUUID();
    Long modelId = 1560002280L;
    Long existingProductId = 2001L;
    Long newProductId = 2002L;

    Item item = new Item();
    item.setName("Item");
    item.setOrganizationId(organizationId);
    item.setOzonModelId(modelId);
    item = itemRepository.save(item);

    ItemVariant existingVariant = new ItemVariant();
    existingVariant.setSku("2001");
    existingVariant.setOzonProductId(existingProductId);
    existingVariant.setItem(item);
    existingVariant.setPrice(BigDecimal.ONE);
    existingVariant.setCurrency(Currency.getInstance("USD"));
    existingVariant = itemVariantRepository.save(existingVariant);

    entityManager.flush();
    entityManager.clear();

    invokeSaveItemWithVariants(organizationId, modelId,
        List.of(
            ozonProduct(existingProductId, modelId, "Updated item", 3001L, "BAR-1", "10.00"),
            ozonProduct(newProductId, modelId, "Updated item", 3002L, "BAR-2", "20.00")),
        Map.of(existingProductId, "Updated description"));

    entityManager.flush();
    entityManager.clear();

    ItemVariant updatedExisting = itemVariantRepository.findById(existingVariant.getId())
        .orElseThrow();
    ItemVariant createdVariant = itemVariantRepository.findByOzonProductId(newProductId)
        .orElseThrow();

    assertEquals("3001", updatedExisting.getSku());
    assertEquals("3002", createdVariant.getSku());
    assertEquals(item.getId(), updatedExisting.getItem().getId());
    assertEquals(item.getId(), createdVariant.getItem().getId());
    assertNotNull(createdVariant.getId());
  }

  private void invokeSaveItemWithVariants(UUID organizationId, Long modelId,
      List<OzonProductInfoResponse.Item> products, Map<Long, String> descriptions)
      throws ReflectiveOperationException {
    Method method = OzonServiceImpl.class.getDeclaredMethod("saveItemWithVariants",
        UUID.class, Long.class, List.class, Map.class);
    method.setAccessible(true);
    method.invoke(ozonService, organizationId, modelId, products, descriptions);
  }

  private OzonProductInfoResponse.Item ozonProduct(Long productId, Long modelId, String name,
      Long sku, String barcode, String price) {
    OzonProductInfoResponse.Item product = new OzonProductInfoResponse.Item();
    product.setId(productId);
    product.setName(name);
    product.setSku(sku);
    product.setBarcodes(List.of(barcode));
    product.setPrice(price);
    product.setCurrencyCode("USD");

    OzonProductInfoResponse.Item.ModelInfo modelInfo =
        new OzonProductInfoResponse.Item.ModelInfo();
    modelInfo.setModelId(modelId);
    product.setModelInfo(modelInfo);
    return product;
  }
}
