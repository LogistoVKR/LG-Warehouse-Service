package kz.logisto.lgwarehouseservice.service.impl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.logisto.lgwarehouseservice.data.dto.PageResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductDescriptionRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductDescriptionResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductInfoRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductInfoResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductListRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductListResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonSubscriptionRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonSubscriptionResponse;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.model.OrganizationModel;
import kz.logisto.lgwarehouseservice.data.model.OzonApiKeyModel;
import kz.logisto.lgwarehouseservice.data.model.OzonSubscriptionModel;
import kz.logisto.lgwarehouseservice.data.model.OzonSubscriptionVariantModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.mapper.ItemMapper;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.OzonService;
import kz.logisto.lgwarehouseservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OzonServiceImpl implements OzonService {

  private static final int BATCH_SIZE = 100;
  private static final int DESCRIPTION_MAX_LENGTH = 255;
  private static final String HEADER_API_KEY = "Api-Key";
  private static final String OZON_VISIBILITY_ALL = "ALL";
  private static final String HEADER_CLIENT_ID = "Client-Id";
  private static final String OZON_PRODUCT_LIST_URI = "/v3/product/list";
  private static final String OZON_PRODUCT_INFO_URI = "/v3/product/info/list";
  private static final String OZON_PRODUCT_DESCRIPTION_URI = "/v1/product/info/description";
  private static final String OZON_PRODUCT_SUBSCRIPTION_URI = "/v1/product/info/subscription";

  private final ItemMapper itemMapper;
  private final UserService userService;
  private final RestClient ozonRestClient;
  private final AccessService accessService;
  private final ItemRepository itemRepository;
  private final ItemVariantMapper itemVariantMapper;
  private final ItemVariantRepository itemVariantRepository;
  private final TransactionTemplate transactionTemplate;

  @Override
  public void syncProducts(UUID organizationId, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);

    OzonApiKeyModel apiKey = userService.getOzonApiKeyByOrganizationId(organizationId);
    if (apiKey == null || !apiKey.isHasIntegration()) {
      log.info("Ozon integration is not configured for organization {}", organizationId);
      return;
    }

    doSync(organizationId, apiKey);
  }

  @Override
  public void syncAllProducts() {
    PageResponse<OrganizationModel> organizations;
    PageRequest pageRequest = PageRequest.of(0, 10);
    do {
      organizations = userService.getOrganizations(pageRequest);

      for (final OrganizationModel organization : organizations.getContent()) {
        OzonApiKeyModel apiKeyModel = userService.getOzonApiKeyByOrganizationId(
            organization.getId());
        doSync(organization.getId(), apiKeyModel);
      }

      pageRequest = pageRequest.next();
    } while (!organizations.isLast());
  }

  @Override
  public List<OzonSubscriptionModel> getSubscriptions(UUID organizationId, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);

    OzonApiKeyModel apiKey = userService.getOzonApiKeyByOrganizationId(organizationId);
    if (apiKey == null || !apiKey.isHasIntegration()) {
      log.info("Ozon integration is not configured for organization {}", organizationId);
      return List.of();
    }

    List<Item> ozonItems = itemRepository.findAllByOrganizationIdAndOzonModelIdIsNotNull(
        organizationId);
    if (ozonItems.isEmpty()) {
      return List.of();
    }

    List<String> allSkus = ozonItems.stream()
        .flatMap(item -> item.getVariants().stream())
        .filter(v -> v.getOzonProductId() != null)
        .map(ItemVariant::getSku)
        .toList();

    if (allSkus.isEmpty()) {
      return List.of();
    }

    Map<Long, Long> countsBySku = fetchSubscriptionCounts(
        apiKey.getOzonClientId(), apiKey.getOzonApiKey(), allSkus);

    List<OzonSubscriptionModel> result = new ArrayList<>();
    for (Item item : ozonItems) {
      List<OzonSubscriptionVariantModel> matchingVariants = item.getVariants().stream()
          .filter(v -> v.getOzonProductId() != null)
          .filter(v -> {
            Long count = countsBySku.get(Long.parseLong(v.getSku()));
            return count != null && count > 0;
          })
          .map(v -> new OzonSubscriptionVariantModel(
              itemVariantMapper.toModel(v),
              countsBySku.get(Long.parseLong(v.getSku()))))
          .toList();

      if (!matchingVariants.isEmpty()) {
        result.add(new OzonSubscriptionModel(itemMapper.toModel(item), matchingVariants));
      }
    }

    return result;
  }

  private void doSync(UUID organizationId, OzonApiKeyModel apiKey) {
    String clientId = apiKey.getOzonClientId();
    String apiKeyStr = apiKey.getOzonApiKey();

    List<Long> productIds = fetchAllProductIds(clientId, apiKeyStr);
    if (productIds.isEmpty()) {
      log.info("No products found in Ozon for organization {}", organizationId);
      return;
    }

    List<OzonProductInfoResponse.Item> products = fetchProductDetails(clientId, apiKeyStr,
        productIds);
    Map<Long, String> descriptions = fetchDescriptions(clientId, apiKeyStr, products);

    Map<Long, List<OzonProductInfoResponse.Item>> byModel = products.stream()
        .filter(p -> p.getModelInfo() != null && p.getModelInfo().getModelId() != null)
        .collect(Collectors.groupingBy(p -> p.getModelInfo().getModelId()));

    for (Map.Entry<Long, List<OzonProductInfoResponse.Item>> entry : byModel.entrySet()) {
      try {
        saveItemWithVariants(organizationId, entry.getKey(), entry.getValue(), descriptions);
      } catch (Exception e) {
        log.error("Failed to save model_id={} for organization {}: {}",
            entry.getKey(), organizationId, e.getMessage());
      }
    }

    log.info("Ozon sync completed for organization {}: {} products, {} models",
        organizationId, products.size(), byModel.size());
  }

  private List<Long> fetchAllProductIds(String clientId, String apiKey) {
    List<Long> allIds = new ArrayList<>();
    String lastId = "";

    do {
      OzonProductListRequest request = new OzonProductListRequest(
          new OzonProductListRequest.Filter(OZON_VISIBILITY_ALL), lastId, BATCH_SIZE);
      try {
        OzonProductListResponse response = ozonRestClient.post()
            .uri(OZON_PRODUCT_LIST_URI)
            .header(HEADER_CLIENT_ID, clientId)
            .header(HEADER_API_KEY, apiKey)
            .body(request)
            .retrieve()
            .body(OzonProductListResponse.class);

        if (response == null || response.getResult() == null
            || response.getResult().getItems() == null
            || response.getResult().getItems().isEmpty()) {
          break;
        }

        response.getResult().getItems().stream()
            .map(OzonProductListResponse.Result.Item::getProductId)
            .forEach(allIds::add);

        lastId = response.getResult().getLastId();
        if (lastId == null || lastId.isBlank()) {
          break;
        }
      } catch (RestClientException e) {
        log.error("Failed to fetch product list from Ozon (last_id={}): {}", lastId,
            e.getMessage());
        break;
      }
    } while (true);

    return allIds;
  }

  private List<OzonProductInfoResponse.Item> fetchProductDetails(String clientId, String apiKey,
      List<Long> productIds) {
    List<OzonProductInfoResponse.Item> result = new ArrayList<>();

    for (int i = 0; i < productIds.size(); i += BATCH_SIZE) {
      List<Long> batch = productIds.subList(i, Math.min(i + BATCH_SIZE, productIds.size()));
      try {
        OzonProductInfoResponse response = ozonRestClient.post()
            .uri(OZON_PRODUCT_INFO_URI)
            .header(HEADER_CLIENT_ID, clientId)
            .header(HEADER_API_KEY, apiKey)
            .body(new OzonProductInfoRequest(batch))
            .retrieve()
            .body(OzonProductInfoResponse.class);

        if (response != null && response.getItems() != null) {
          result.addAll(response.getItems());
        }
      } catch (RestClientException e) {
        log.error("Failed to fetch product details for batch starting at index {}: {}", i,
            e.getMessage());
      }
    }

    return result;
  }

  private Map<Long, String> fetchDescriptions(String clientId, String apiKey,
      List<OzonProductInfoResponse.Item> products) {
    Map<Long, String> descriptions = new HashMap<>();

    for (OzonProductInfoResponse.Item product : products) {
      try {
        OzonProductDescriptionResponse response = ozonRestClient.post()
            .uri(OZON_PRODUCT_DESCRIPTION_URI)
            .header(HEADER_CLIENT_ID, clientId)
            .header(HEADER_API_KEY, apiKey)
            .body(new OzonProductDescriptionRequest(product.getId()))
            .retrieve()
            .body(OzonProductDescriptionResponse.class);

        if (response != null && response.getResult() != null
            && response.getResult().getDescription() != null) {
          descriptions.put(product.getId(), response.getResult().getDescription());
        }
      } catch (RestClientException e) {
        log.warn("Failed to fetch description for product_id={}: {}", product.getId(),
            e.getMessage());
      }
    }

    return descriptions;
  }

  private Map<Long, Long> fetchSubscriptionCounts(String clientId, String apiKey,
      List<String> skus) {
    Map<Long, Long> result = new HashMap<>();
    try {
      OzonSubscriptionResponse response = ozonRestClient.post()
          .uri(OZON_PRODUCT_SUBSCRIPTION_URI)
          .header(HEADER_CLIENT_ID, clientId)
          .header(HEADER_API_KEY, apiKey)
          .body(new OzonSubscriptionRequest(skus))
          .retrieve()
          .body(OzonSubscriptionResponse.class);
      if (response != null && response.getResult() != null) {
        response.getResult().forEach(r -> result.put(r.getSku(), r.getCount()));
      }
    } catch (RestClientException e) {
      log.error("Failed to fetch subscription counts: {}", e.getMessage());
    }
    return result;
  }

  private void saveItemWithVariants(UUID organizationId, Long modelId,
      List<OzonProductInfoResponse.Item> products, Map<Long, String> descriptions) {
    transactionTemplate.executeWithoutResult(
        status -> doSaveItemWithVariants(organizationId, modelId, products, descriptions));
  }

  private void doSaveItemWithVariants(UUID organizationId, Long modelId,
      List<OzonProductInfoResponse.Item> products, Map<Long, String> descriptions) {
    OzonProductInfoResponse.Item first = products.getFirst();
    String rawDescription = descriptions.get(first.getId());

    Item item = itemRepository
        .findByOrganizationIdAndOzonModelId(organizationId, modelId)
        .orElse(new Item());

    item.setOzonModelId(modelId);
    item.setOrganizationId(organizationId);
    item.setName(first.getName());
    item.setDescription(truncate(rawDescription, DESCRIPTION_MAX_LENGTH));
    if (item.getId() == null) {
      item = itemRepository.save(item);
    }

    for (OzonProductInfoResponse.Item product : products) {
      ItemVariant variant = itemVariantRepository
          .findByOzonProductId(product.getId())
          .orElse(new ItemVariant());

      variant.setItem(item);
      variant.setOzonProductId(product.getId());
      variant.setSku(Long.toString(product.getSku()));
      variant.setBarcode(
          product.getBarcodes() != null && !product.getBarcodes().isEmpty()
              ? product.getBarcodes().getFirst() : null);
      variant.setPrice(new BigDecimal(product.getPrice()));
      variant.setCurrency(Currency.getInstance(product.getCurrencyCode()));

      if (variant.getId() == null) {
        itemVariantRepository.save(variant);
      }
    }
  }

  public static String truncate(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    return value.length() > maxLength ? value.substring(0, maxLength) : value;
  }
}
