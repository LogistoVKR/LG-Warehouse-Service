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
import kz.logisto.lgwarehouseservice.config.property.RestProperty;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductDescriptionRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductDescriptionResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductInfoRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductInfoResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductListRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductListResponse;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.model.OzonApiKeyModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.OzonService;
import kz.logisto.lgwarehouseservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
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

  private final UserService userService;
  private final RestProperty restProperty;
  private final AccessService accessService;
  private final ItemRepository itemRepository;
  private final ItemVariantRepository itemVariantRepository;

  @Override
  public void syncProducts(UUID organizationId, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);

    OzonApiKeyModel apiKey = userService.getOzonApiKeyByOrganizationId(organizationId);
    if (apiKey == null || !apiKey.isHasIntegration()) {
      log.info("Ozon integration is not configured for organization {}", organizationId);
      return;
    }

    RestClient client = buildOzonClient(apiKey.getOzonClientId(), apiKey.getOzonApiKey());

    List<Long> productIds = fetchAllProductIds(client);
    if (productIds.isEmpty()) {
      log.info("No products found in Ozon for organization {}", organizationId);
      return;
    }

    List<OzonProductInfoResponse.Item> products = fetchProductDetails(client, productIds);
    Map<Long, String> descriptions = fetchDescriptions(client, products);

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

  private List<Long> fetchAllProductIds(RestClient client) {
    List<Long> allIds = new ArrayList<>();
    String lastId = "";

    do {
      OzonProductListRequest request = new OzonProductListRequest(
          new OzonProductListRequest.Filter(OZON_VISIBILITY_ALL), lastId, BATCH_SIZE);
      try {
        OzonProductListResponse response = client.post()
            .uri(OZON_PRODUCT_LIST_URI)
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

  private List<OzonProductInfoResponse.Item> fetchProductDetails(RestClient client,
      List<Long> productIds) {
    List<OzonProductInfoResponse.Item> result = new ArrayList<>();

    for (int i = 0; i < productIds.size(); i += BATCH_SIZE) {
      List<Long> batch = productIds.subList(i, Math.min(i + BATCH_SIZE, productIds.size()));
      try {
        OzonProductInfoResponse response = client.post()
            .uri(OZON_PRODUCT_INFO_URI)
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

  private Map<Long, String> fetchDescriptions(RestClient client,
      List<OzonProductInfoResponse.Item> products) {
    Map<Long, String> descriptions = new HashMap<>();

    for (OzonProductInfoResponse.Item product : products) {
      try {
        OzonProductDescriptionResponse response = client.post()
            .uri(OZON_PRODUCT_DESCRIPTION_URI)
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

  private void saveItemWithVariants(UUID organizationId, Long modelId,
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
    item = itemRepository.save(item);

    for (OzonProductInfoResponse.Item product : products) {
      ItemVariant variant = itemVariantRepository
          .findByOzonProductId(product.getId())
          .orElse(new ItemVariant());

      variant.setItem(item);
      variant.setOzonProductId(product.getId());
      variant.setSku(String.valueOf(product.getSku()));
      variant.setBarcode(
          product.getBarcodes() != null && !product.getBarcodes().isEmpty()
              ? product.getBarcodes().get(0) : null);
      variant.setPrice(new BigDecimal(product.getPrice()));
      variant.setCurrency(Currency.getInstance(product.getCurrencyCode()));

      itemVariantRepository.save(variant);
    }
  }

  public static String truncate(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    return value.length() > maxLength ? value.substring(0, maxLength) : value;
  }

  private RestClient buildOzonClient(String clientId, String apiKey) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(restProperty.getOzonApi().getConnectTimeout());
    factory.setReadTimeout(restProperty.getOzonApi().getReadTimeout());
    return RestClient.builder()
        .baseUrl(restProperty.getOzonApi().getUrl())
        .requestFactory(factory)
        .requestInterceptor((request, body, execution) -> {
          request.getHeaders().set(HEADER_CLIENT_ID, clientId);
          request.getHeaders().set(HEADER_API_KEY, apiKey);
          return execution.execute(request, body);
        })
        .build();
  }
}
