package kz.logisto.lgwarehouseservice.service.impl;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import kz.logisto.lgwarehouseservice.data.dto.PageResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonAvailableWarehousesResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonPostingFboListResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonPostingFbsListResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonPostingListRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductDescriptionRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductDescriptionResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductInfoRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductInfoResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductListRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonProductListResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonReturnsListRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonReturnsListResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonStockOnWarehousesRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonStockOnWarehousesResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonStocksUpdateRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonStocksUpdateResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonSubscriptionRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonSubscriptionResponse;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonWarehouseListRequest;
import kz.logisto.lgwarehouseservice.data.dto.ozon.OzonWarehouseListResponse;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantMovement;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantPointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.key.ItemVariantPointOfStorageId;
import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import kz.logisto.lgwarehouseservice.data.enums.PointOfStorageType;
import kz.logisto.lgwarehouseservice.data.model.OrganizationModel;
import kz.logisto.lgwarehouseservice.data.model.OzonApiKeyModel;
import kz.logisto.lgwarehouseservice.data.model.OzonSubscriptionModel;
import kz.logisto.lgwarehouseservice.data.model.OzonSubscriptionVariantModel;
import kz.logisto.lgwarehouseservice.data.model.WarehouseAvailabilityModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantMovementRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantPointOfStorageRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.data.repository.PointOfStorageRepository;
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
  private static final int POSTING_PAGE_SIZE = 50;
  private static final int DESCRIPTION_MAX_LENGTH = 255;
  private static final String HEADER_API_KEY = "Api-Key";
  private static final String OZON_VISIBILITY_ALL = "ALL";
  private static final String HEADER_CLIENT_ID = "Client-Id";
  private static final String OZON_PRODUCT_LIST_URI = "/v3/product/list";
  private static final String OZON_WAREHOUSE_LIST_URI = "/v2/warehouse/list";
  private static final String OZON_AVAILABLE_WAREHOUSES_URI = "/v1/supplier/available_warehouses";
  private static final String OZON_PRODUCT_INFO_URI = "/v3/product/info/list";
  private static final String OZON_PRODUCT_DESCRIPTION_URI = "/v1/product/info/description";
  private static final String OZON_PRODUCT_SUBSCRIPTION_URI = "/v1/product/info/subscription";
  private static final String OZON_STOCKS_UPDATE_URI = "/v2/products/stocks";
  private static final String OZON_POSTING_FBO_LIST_URI = "/v2/posting/fbo/list";
  private static final String OZON_POSTING_FBS_LIST_URI = "/v3/posting/fbs/list";
  private static final String OZON_RETURNS_LIST_URI = "/v1/returns/list";
  private static final String OZON_STOCK_ON_WAREHOUSES_URI = "/v2/analytics/stock_on_warehouses";

  private static final long ROLLING_WINDOW_MINUTES = 30;
  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

  private final Map<UUID, Instant> lastPostingSync = new ConcurrentHashMap<>();
  private final Map<UUID, Instant> lastReturnsSync = new ConcurrentHashMap<>();

  private final ItemMapper itemMapper;
  private final UserService userService;
  private final RestClient ozonRestClient;
  private final AccessService accessService;
  private final ItemRepository itemRepository;
  private final ItemVariantMapper itemVariantMapper;
  private final ItemVariantRepository itemVariantRepository;
  private final PointOfStorageRepository pointOfStorageRepository;
  private final ItemVariantMovementRepository movementRepository;
  private final ItemVariantPointOfStorageRepository stockRepository;
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

  @Override
  public void syncWarehouses(UUID organizationId, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);

    OzonApiKeyModel apiKey = userService.getOzonApiKeyByOrganizationId(organizationId);
    if (apiKey == null || !apiKey.isHasIntegration()) {
      log.info("Ozon integration is not configured for organization {}", organizationId);
      return;
    }

    doSyncWarehouses(organizationId, apiKey);
  }

  @Override
  public void syncAllWarehouses() {
    PageResponse<OrganizationModel> organizations;
    PageRequest pageRequest = PageRequest.of(0, 10);
    do {
      organizations = userService.getOrganizations(pageRequest);

      for (final OrganizationModel organization : organizations.getContent()) {
        OzonApiKeyModel apiKeyModel = userService.getOzonApiKeyByOrganizationId(
            organization.getId());
        doSyncWarehouses(organization.getId(), apiKeyModel);
      }

      pageRequest = pageRequest.next();
    } while (!organizations.isLast());
  }

  @Override
  public void pushVariantStockToOzon(UUID organizationId, UUID itemVariantId,
      UUID pointOfStorageId) {
    try {
      OzonApiKeyModel apiKey = userService.getOzonApiKeyByOrganizationId(organizationId);
      if (apiKey == null || !apiKey.isHasIntegration()) {
        return;
      }

      ItemVariant variant = itemVariantRepository.findById(itemVariantId).orElse(null);
      if (variant == null || variant.getOzonProductId() == null) {
        return;
      }

      PointOfStorage warehouse = pointOfStorageRepository.findById(pointOfStorageId).orElse(null);
      if (warehouse == null || warehouse.getOzonWarehouseId() == null) {
        return;
      }

      ItemVariantPointOfStorageId key = new ItemVariantPointOfStorageId(
          itemVariantId, pointOfStorageId);
      ItemVariantPointOfStorage stock = stockRepository.findById(key).orElse(null);
      int quantity = stock != null ? Math.max(0, stock.getQuantity()) : 0;

      pushStocksBatch(
          apiKey.getOzonClientId(), apiKey.getOzonApiKey(),
          List.of(new OzonStocksUpdateRequest.StockItem(
              variant.getOzonProductId(), quantity, warehouse.getOzonWarehouseId())),
          organizationId);

    } catch (Exception e) {
      log.error("Failed to push stock to Ozon: variant={}, warehouse={}, org={}: {}",
          itemVariantId, pointOfStorageId, organizationId, e.getMessage());
    }
  }

  @Override
  public void syncStocksToOzon(UUID organizationId, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);

    OzonApiKeyModel apiKey = userService.getOzonApiKeyByOrganizationId(organizationId);
    if (apiKey == null || !apiKey.isHasIntegration()) {
      log.info("Ozon integration is not configured for organization {}", organizationId);
      return;
    }

    doSyncStocksToOzon(organizationId, apiKey);
  }

  @Override
  public void syncAllStocksToOzon() {
    PageResponse<OrganizationModel> organizations;
    PageRequest pageRequest = PageRequest.of(0, 10);
    do {
      organizations = userService.getOrganizations(pageRequest);

      for (final OrganizationModel organization : organizations.getContent()) {
        OzonApiKeyModel apiKeyModel = userService.getOzonApiKeyByOrganizationId(
            organization.getId());
        if (apiKeyModel != null && apiKeyModel.isHasIntegration()) {
          doSyncStocksToOzon(organization.getId(), apiKeyModel);
        }
      }

      pageRequest = pageRequest.next();
    } while (!organizations.isLast());
  }

  private void doSyncStocksToOzon(UUID organizationId, OzonApiKeyModel apiKey) {
    List<PointOfStorage> ozonWarehouses = pointOfStorageRepository
        .findAllByOrganizationIdAndOzonWarehouseIdIsNotNull(organizationId);

    if (ozonWarehouses.isEmpty()) {
      log.info("No Ozon-linked warehouses for organization {}", organizationId);
      return;
    }

    List<OzonStocksUpdateRequest.StockItem> stockItems = new ArrayList<>();

    for (final PointOfStorage warehouse : ozonWarehouses) {
      List<ItemVariantPointOfStorage> stocks = stockRepository
          .findByIdPointOfStorageId(warehouse.getId());

      for (ItemVariantPointOfStorage stock : stocks) {
        ItemVariant variant = itemVariantRepository
            .findById(stock.getId().getItemVariantId())
            .orElse(null);

        if (variant == null || variant.getOzonProductId() == null) {
          continue;
        }

        if (stock.getQuantity() <= 0) {
          continue;
        }

        stockItems.add(new OzonStocksUpdateRequest.StockItem(
            variant.getOzonProductId(),
            stock.getQuantity(),
            warehouse.getOzonWarehouseId()
        ));
      }
    }

    if (stockItems.isEmpty()) {
      return;
    }

    for (int i = 0; i < stockItems.size(); i += BATCH_SIZE) {
      List<OzonStocksUpdateRequest.StockItem> batch = stockItems.subList(
          i, Math.min(i + BATCH_SIZE, stockItems.size()));
      pushStocksBatch(apiKey.getOzonClientId(), apiKey.getOzonApiKey(), batch, organizationId);
    }

    log.info("Stock push completed for organization {}: {} items", organizationId,
        stockItems.size());
  }

  private void pushStocksBatch(String clientId, String apiKey,
      List<OzonStocksUpdateRequest.StockItem> batch, UUID organizationId) {
    try {
      OzonStocksUpdateResponse response = ozonRestClient.post()
          .uri(OZON_STOCKS_UPDATE_URI)
          .header(HEADER_CLIENT_ID, clientId)
          .header(HEADER_API_KEY, apiKey)
          .body(new OzonStocksUpdateRequest(batch))
          .retrieve()
          .body(OzonStocksUpdateResponse.class);

      if (response != null && response.getResult() != null) {
        response.getResult().stream()
            .filter(r -> !r.isUpdated())
            .forEach(r -> log.warn(
                "Ozon stock not updated for product_id={}, org={}, errors={}",
                r.getProductId(), organizationId,
                r.getErrors() == null ? "[]" : r.getErrors().stream()
                    .map(e -> e.getCode() + ": " + e.getMessage())
                    .toList()));
      }
    } catch (RestClientException e) {
      log.error("Failed to push stocks to Ozon for org {}: {}", organizationId, e.getMessage());
    }
  }

  @Override
  public void pullAllPostings() {
    PageResponse<OrganizationModel> organizations;
    PageRequest pageRequest = PageRequest.of(0, 10);
    do {
      organizations = userService.getOrganizations(pageRequest);

      for (final OrganizationModel organization : organizations.getContent()) {
        OzonApiKeyModel apiKeyModel = userService.getOzonApiKeyByOrganizationId(
            organization.getId());
        if (apiKeyModel != null && apiKeyModel.isHasIntegration()) {
          doPullFboPostings(organization.getId(), apiKeyModel);
          doPullFbsPostings(organization.getId(), apiKeyModel);
        }
      }

      pageRequest = pageRequest.next();
    } while (!organizations.isLast());
  }

  private void doPullFboPostings(UUID organizationId, OzonApiKeyModel apiKey) {
    Instant from = lastPostingSync.getOrDefault(organizationId,
        Instant.now().minusSeconds(ROLLING_WINDOW_MINUTES * 60));
    Instant to = Instant.now();

    List<OzonPostingFboListResponse.Posting> postings;
    int offset = 0;
    do {
      postings = fetchFboPostings(apiKey.getOzonClientId(), apiKey.getOzonApiKey(),
          buildPostingRequest(from, to, POSTING_PAGE_SIZE, offset));
      for (OzonPostingFboListResponse.Posting posting : postings) {
        try {
          handleFboPosting(organizationId, posting);
        } catch (Exception e) {
          log.error("Failed to handle FBO posting {} for org {}: {}",
              posting.getPostingNumber(), organizationId, e.getMessage());
        }
      }
      offset += POSTING_PAGE_SIZE;
    } while (postings.size() == POSTING_PAGE_SIZE);

    lastPostingSync.put(organizationId, to);
    log.info("FBO posting pull completed for organization {}", organizationId);
  }

  private void handleFboPosting(UUID organizationId, OzonPostingFboListResponse.Posting posting) {
    if (!"delivered".equals(posting.getStatus())) {
      return;
    }
    String reason = "ozon_fbo_delivered_" + posting.getPostingNumber();
    if (movementRepository.existsByReason(reason)) {
      return;
    }
    Long warehouseId = posting.getAnalyticsData() != null
        ? posting.getAnalyticsData().getWarehouseId() : null;
    PointOfStorage warehouse = resolveWarehouse(organizationId, warehouseId);
    if (warehouse == null) {
      log.warn("No warehouse found for FBO posting {}, warehouseId={}",
          posting.getPostingNumber(), warehouseId);
      return;
    }
    createSaleMovements(organizationId, posting.getProducts().stream()
        .map(p -> new PostingProduct(p.getSku(), p.getQuantity(), null, null))
        .toList(), warehouse, reason);
  }

  private void doPullFbsPostings(UUID organizationId, OzonApiKeyModel apiKey) {
    Instant from = lastPostingSync.getOrDefault(organizationId,
        Instant.now().minusSeconds(ROLLING_WINDOW_MINUTES * 60));
    Instant to = Instant.now();

    List<OzonPostingFbsListResponse.Posting> postings;
    int offset = 0;
    do {
      postings = fetchFbsPostings(apiKey.getOzonClientId(), apiKey.getOzonApiKey(),
          buildPostingRequest(from, to, POSTING_PAGE_SIZE, offset));
      for (OzonPostingFbsListResponse.Posting posting : postings) {
        try {
          handleFbsPosting(organizationId, posting);
        } catch (Exception e) {
          log.error("Failed to handle FBS posting {} for org {}: {}",
              posting.getPostingNumber(), organizationId, e.getMessage());
        }
      }
      offset += POSTING_PAGE_SIZE;
    } while (postings.size() == POSTING_PAGE_SIZE);

    log.info("FBS posting pull completed for organization {}", organizationId);
  }

  private void handleFbsPosting(UUID organizationId, OzonPostingFbsListResponse.Posting posting) {
    String status = posting.getStatus();
    String postingNumber = posting.getPostingNumber();

    PointOfStorage warehouse = resolveWarehouse(organizationId, posting.getWarehouseId());

    if ("awaiting_packaging".equals(status)) {
      String reason = "ozon_fbs_reserve_" + postingNumber;
      if (movementRepository.existsByReason(reason) || warehouse == null) {
        return;
      }
      createReserveMovements(organizationId, posting.getProducts().stream()
          .map(p -> new PostingProduct(p.getSku(), p.getQuantity(), p.getPrice(),
              p.getCurrencyCode()))
          .toList(), warehouse, reason);

    } else if ("delivered".equals(status)) {
      String saleReason = "ozon_fbs_delivered_" + postingNumber;
      if (!movementRepository.existsByReason(saleReason) && warehouse != null) {
        String reserveReason = "ozon_fbs_reserve_" + postingNumber;
        if (movementRepository.existsByReason(reserveReason)) {
          releaseReserve(organizationId, posting.getProducts().stream()
              .map(p -> new PostingProduct(p.getSku(), p.getQuantity(), null, null))
              .toList(), warehouse, "ozon_fbs_reserve_released_" + postingNumber);
        }
        createSaleMovements(organizationId, posting.getProducts().stream()
            .map(p -> new PostingProduct(p.getSku(), p.getQuantity(), p.getPrice(),
                p.getCurrencyCode()))
            .toList(), warehouse, saleReason);
      }

    } else if ("cancelled".equals(status)) {
      String reserveReason = "ozon_fbs_reserve_" + postingNumber;
      String cancelReason = "ozon_fbs_cancelled_" + postingNumber;
      if (movementRepository.existsByReason(reserveReason)
          && !movementRepository.existsByReason(cancelReason)
          && warehouse != null) {
        releaseReserve(organizationId, posting.getProducts().stream()
            .map(p -> new PostingProduct(p.getSku(), p.getQuantity(), null, null))
            .toList(), warehouse, cancelReason);
      }
    }
  }

  @Override
  public void pullAllReturns() {
    PageResponse<OrganizationModel> organizations;
    PageRequest pageRequest = PageRequest.of(0, 10);
    do {
      organizations = userService.getOrganizations(pageRequest);

      for (final OrganizationModel organization : organizations.getContent()) {
        OzonApiKeyModel apiKeyModel = userService.getOzonApiKeyByOrganizationId(
            organization.getId());
        if (apiKeyModel != null && apiKeyModel.isHasIntegration()) {
          doPullReturns(organization.getId(), apiKeyModel);
        }
      }

      pageRequest = pageRequest.next();
    } while (!organizations.isLast());
  }

  private void doPullReturns(UUID organizationId, OzonApiKeyModel apiKey) {
    Instant from = lastReturnsSync.getOrDefault(organizationId,
        Instant.now().minusSeconds(ROLLING_WINDOW_MINUTES * 60));
    Instant to = Instant.now();
    String fromStr = ISO_FORMATTER.format(from);
    String toStr = ISO_FORMATTER.format(to);

    long lastId = 0;
    OzonReturnsListResponse response;
    do {
      response = fetchReturnsListResponse(
          apiKey.getOzonClientId(), apiKey.getOzonApiKey(),
          new OzonReturnsListRequest(
              new OzonReturnsListRequest.Filter(
                  new OzonReturnsListRequest.TimeRange(fromStr, toStr)),
              BATCH_SIZE, lastId));

      if (response == null || response.getReturns() == null) {
        break;
      }

      for (OzonReturnsListResponse.Return ret : response.getReturns()) {
        try {
          handleReturn(organizationId, ret);
        } catch (Exception e) {
          log.error("Failed to handle return id={} for org {}: {}",
              ret.getId(), organizationId, e.getMessage());
        }
        lastId = Math.max(lastId, ret.getId());
      }
    } while (response.isHasNext());

    lastReturnsSync.put(organizationId, to);
    log.info("Returns pull completed for organization {}", organizationId);
  }

  private void handleReturn(UUID organizationId, OzonReturnsListResponse.Return ret) {
    if (ret.getProduct() == null || ret.getProduct().getSku() == null) {
      return;
    }

    String reason = "ozon_return_" + ret.getId();
    if (movementRepository.existsByReason(reason)) {
      return;
    }

    Long warehouseId = ret.getPlace() != null ? ret.getPlace().getId() : null;
    PointOfStorage warehouse = resolveWarehouse(organizationId, warehouseId);
    if (warehouse == null) {
      List<PointOfStorage> ozonWarehouses = pointOfStorageRepository
          .findAllByOrganizationIdAndOzonWarehouseIdIsNotNull(organizationId);
      if (ozonWarehouses.isEmpty()) {
        log.warn("No warehouse found for return id={}, org={}", ret.getId(), organizationId);
        return;
      }
      warehouse = ozonWarehouses.getFirst();
    }

    final PointOfStorage finalWarehouse = warehouse;
    transactionTemplate.executeWithoutResult(tx -> {
      ItemVariant variant = itemVariantRepository
          .findBySku(String.valueOf(ret.getProduct().getSku()))
          .orElse(null);
      if (variant == null) {
        return;
      }
      stockRepository.increment(variant.getId(), finalWarehouse.getId(),
          ret.getProduct().getQuantity());
      saveMovement(organizationId, null, finalWarehouse, variant,
          ret.getProduct().getQuantity(), MovementType.RETURN, reason, null, null);
    });
  }

  @Override
  public void reconcileAllStocks() {
    PageResponse<OrganizationModel> organizations;
    PageRequest pageRequest = PageRequest.of(0, 10);
    do {
      organizations = userService.getOrganizations(pageRequest);

      for (final OrganizationModel organization : organizations.getContent()) {
        OzonApiKeyModel apiKeyModel = userService.getOzonApiKeyByOrganizationId(
            organization.getId());
        if (apiKeyModel != null && apiKeyModel.isHasIntegration()) {
          doReconcileStocks(organization.getId(), apiKeyModel);
        }
      }

      pageRequest = pageRequest.next();
    } while (!organizations.isLast());
  }

  private void doReconcileStocks(UUID organizationId, OzonApiKeyModel apiKey) {
    String today = LocalDate.now(ZoneOffset.UTC).toString();

    List<OzonStockOnWarehousesResponse.Row> rows;
    int offset = 0;
    do {
      rows = fetchStockOnWarehouses(apiKey.getOzonClientId(), apiKey.getOzonApiKey(),
          new OzonStockOnWarehousesRequest(BATCH_SIZE, offset, "ALL"));
      for (final OzonStockOnWarehousesResponse.Row row : rows) {
        try {
          reconcileRow(organizationId, row, today);
        } catch (Exception e) {
          log.error("Reconciliation failed for sku={}, warehouseId={}, org={}: {}",
              row.getSku(), row.getWarehouseId(), organizationId, e.getMessage());
        }
      }
      offset += BATCH_SIZE;
    } while (rows.size() == BATCH_SIZE);

    log.info("Reconciliation completed for organization {}", organizationId);
  }

  private void reconcileRow(UUID organizationId, OzonStockOnWarehousesResponse.Row row,
      String today) {
    ItemVariant variant = itemVariantRepository
        .findBySku(String.valueOf(row.getSku()))
        .orElse(null);
    if (variant == null) {
      return;
    }

    PointOfStorage warehouse = resolveWarehouse(organizationId, row.getWarehouseId());
    if (warehouse == null) {
      return;
    }

    ItemVariantPointOfStorageId compositeKey = new ItemVariantPointOfStorageId(
        variant.getId(), warehouse.getId());
    Optional<ItemVariantPointOfStorage> stockOpt = stockRepository.findById(compositeKey);
    int systemQuantity = stockOpt.map(ItemVariantPointOfStorage::getQuantity).orElse(0);

    int delta = row.getFreeToSellAmount() - systemQuantity;
    if (delta == 0) {
      return;
    }

    String reason =
        "ozon_reconciliation_" + row.getSku() + "_" + row.getWarehouseId() + "_" + today;
    if (movementRepository.existsByReason(reason)) {
      return;
    }

    transactionTemplate.executeWithoutResult(tx -> {
      if (delta > 0) {
        stockRepository.increment(variant.getId(), warehouse.getId(), delta);
        saveMovement(organizationId, null, warehouse, variant,
            delta, MovementType.PURCHASE, reason, null, null);
      } else {
        stockRepository.decrement(variant.getId(), warehouse.getId(), -delta);
        saveMovement(organizationId, warehouse, null, variant,
            -delta, MovementType.WRITE_OFF, reason, null, null);
      }
    });

    log.info("Reconciliation adjusted sku={} at warehouse={}: delta={}", row.getSku(),
        row.getWarehouseId(), delta);
  }

  private void createSaleMovements(UUID organizationId, List<PostingProduct> products,
      PointOfStorage warehouse, String reason) {
    transactionTemplate.executeWithoutResult(tx -> {
      for (PostingProduct product : products) {
        ItemVariant variant = itemVariantRepository
            .findBySku(String.valueOf(product.sku()))
            .orElse(null);
        if (variant == null) {
          continue;
        }
        ItemVariantPointOfStorageId key = new ItemVariantPointOfStorageId(
            variant.getId(), warehouse.getId());
        ItemVariantPointOfStorage stock = stockRepository.findById(key).orElse(null);
        if (stock == null || stock.getQuantity() <= 0) {
          log.warn("Skipping SALE for variant={} at warehouse={}: stock not initialized or 0",
              variant.getId(), warehouse.getId());
          continue;
        }
        stockRepository.decrement(variant.getId(), warehouse.getId(), product.quantity());
        BigDecimal price = product.price();
        Currency currency = parseCurrency(product.currencyCode());
        saveMovement(organizationId, warehouse, null, variant,
            product.quantity(), MovementType.SALE, reason, price, currency);
      }
    });
  }

  private void createReserveMovements(UUID organizationId, List<PostingProduct> products,
      PointOfStorage warehouse, String reason) {
    transactionTemplate.executeWithoutResult(tx -> {
      for (PostingProduct product : products) {
        ItemVariant variant = itemVariantRepository
            .findBySku(String.valueOf(product.sku()))
            .orElse(null);
        if (variant == null) {
          continue;
        }
        ItemVariantPointOfStorageId key = new ItemVariantPointOfStorageId(
            variant.getId(), warehouse.getId());
        ItemVariantPointOfStorage stock = stockRepository.findById(key).orElse(null);
        if (stock == null || stock.getQuantity() <= 0) {
          log.warn("Skipping RESERVE for variant={} at warehouse={}: stock not initialized or 0",
              variant.getId(), warehouse.getId());
          continue;
        }
        stockRepository.reserve(variant.getId(), warehouse.getId(), product.quantity());
        BigDecimal price = product.price();
        Currency currency = parseCurrency(product.currencyCode());
        saveMovement(organizationId, warehouse, null, variant,
            product.quantity(), MovementType.RESERVE, reason, price, currency);
      }
    });
  }

  private void releaseReserve(UUID organizationId, List<PostingProduct> products,
      PointOfStorage warehouse, String reason) {
    transactionTemplate.executeWithoutResult(tx -> {
      for (PostingProduct product : products) {
        ItemVariant variant = itemVariantRepository
            .findBySku(String.valueOf(product.sku()))
            .orElse(null);
        if (variant == null) {
          continue;
        }
        stockRepository.release(variant.getId(), warehouse.getId(), product.quantity());
        saveMovement(organizationId, null, warehouse, variant,
            product.quantity(), MovementType.PURCHASE, reason, null, null);
      }
    });
  }

  private void saveMovement(UUID organizationId, PointOfStorage from, PointOfStorage to,
      ItemVariant variant, int quantity, MovementType type, String reason,
      BigDecimal price, Currency currency) {
    ItemVariantMovement movement = new ItemVariantMovement();
    movement.setOrganizationId(organizationId);
    movement.setFromPointOfStorage(from);
    movement.setToPointOfStorage(to);
    movement.setItemVariant(variant);
    movement.setQuantity(quantity);
    movement.setType(type);
    movement.setReason(reason);
    movement.setPricePerItem(price);
    movement.setCurrency(currency);
    movement.setCreated(java.time.LocalDateTime.now(ZoneOffset.UTC));
    movementRepository.save(movement);
  }

  private PointOfStorage resolveWarehouse(UUID organizationId, Long ozonWarehouseId) {
    if (ozonWarehouseId == null) {
      return null;
    }
    return pointOfStorageRepository
        .findByOrganizationIdAndOzonWarehouseId(organizationId, ozonWarehouseId)
        .orElse(null);
  }

  private Currency parseCurrency(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    try {
      return Currency.getInstance(code);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private OzonPostingListRequest buildPostingRequest(Instant from, Instant to, int limit,
      int offset) {
    return new OzonPostingListRequest(
        new OzonPostingListRequest.Filter(ISO_FORMATTER.format(from), ISO_FORMATTER.format(to)),
        limit,
        offset,
        new OzonPostingListRequest.With(true)
    );
  }

  private List<OzonPostingFboListResponse.Posting> fetchFboPostings(String clientId, String apiKey,
      OzonPostingListRequest request) {
    try {
      OzonPostingFboListResponse response = ozonRestClient.post()
          .uri(OZON_POSTING_FBO_LIST_URI)
          .header(HEADER_CLIENT_ID, clientId)
          .header(HEADER_API_KEY, apiKey)
          .body(request)
          .retrieve()
          .body(OzonPostingFboListResponse.class);

      if (response != null && response.getResult() != null) {
        return response.getResult();
      }
    } catch (RestClientException e) {
      log.error("Failed to fetch FBO postings from Ozon: {}", e.getMessage());
    }
    return List.of();
  }

  private List<OzonPostingFbsListResponse.Posting> fetchFbsPostings(String clientId, String apiKey,
      OzonPostingListRequest request) {
    try {
      OzonPostingFbsListResponse response = ozonRestClient.post()
          .uri(OZON_POSTING_FBS_LIST_URI)
          .header(HEADER_CLIENT_ID, clientId)
          .header(HEADER_API_KEY, apiKey)
          .body(request)
          .retrieve()
          .body(OzonPostingFbsListResponse.class);

      if (response != null && response.getResult() != null
          && response.getResult().getPostings() != null) {
        return response.getResult().getPostings();
      }
    } catch (RestClientException e) {
      log.error("Failed to fetch FBS postings from Ozon: {}", e.getMessage());
    }
    return List.of();
  }

  private OzonReturnsListResponse fetchReturnsListResponse(String clientId, String apiKey,
      OzonReturnsListRequest request) {
    try {
      return ozonRestClient.post()
          .uri(OZON_RETURNS_LIST_URI)
          .header(HEADER_CLIENT_ID, clientId)
          .header(HEADER_API_KEY, apiKey)
          .body(request)
          .retrieve()
          .body(OzonReturnsListResponse.class);
    } catch (RestClientException e) {
      log.error("Failed to fetch returns list from Ozon: {}", e.getMessage());
    }
    return null;
  }

  private List<OzonStockOnWarehousesResponse.Row> fetchStockOnWarehouses(String clientId,
      String apiKey, OzonStockOnWarehousesRequest request) {
    try {
      OzonStockOnWarehousesResponse response = ozonRestClient.post()
          .uri(OZON_STOCK_ON_WAREHOUSES_URI)
          .header(HEADER_CLIENT_ID, clientId)
          .header(HEADER_API_KEY, apiKey)
          .body(request)
          .retrieve()
          .body(OzonStockOnWarehousesResponse.class);

      if (response != null && response.getResult() != null
          && response.getResult().getRows() != null) {
        return response.getResult().getRows();
      }
    } catch (RestClientException e) {
      log.error("Failed to fetch stock on warehouses from Ozon: {}", e.getMessage());
    }
    return List.of();
  }

  private void doSyncWarehouses(UUID organizationId, OzonApiKeyModel apiKey) {
    if (apiKey == null || !apiKey.isHasIntegration()) {
      return;
    }

    List<OzonWarehouseListResponse.WarehouseItem> warehouses = fetchWarehouses(
        apiKey.getOzonClientId(), apiKey.getOzonApiKey());

    if (warehouses.isEmpty()) {
      log.info("No warehouses found in Ozon for organization {}", organizationId);
      return;
    }

    for (OzonWarehouseListResponse.WarehouseItem warehouse : warehouses) {
      try {
        PointOfStorage entity = pointOfStorageRepository
            .findByOrganizationIdAndOzonWarehouseId(organizationId, warehouse.getWarehouseId())
            .orElse(new PointOfStorage());

        entity.setOzonWarehouseId(warehouse.getWarehouseId());
        entity.setOrganizationId(organizationId);
        entity.setName(warehouse.getName());
        entity.setType(PointOfStorageType.WAREHOUSE);

        pointOfStorageRepository.save(entity);
      } catch (Exception e) {
        log.error("Failed to save warehouse_id={} for organization {}: {}",
            warehouse.getWarehouseId(), organizationId, e.getMessage());
      }
    }

    log.info("Ozon warehouse sync completed for organization {}: {} warehouses",
        organizationId, warehouses.size());
  }

  private List<OzonWarehouseListResponse.WarehouseItem> fetchWarehouses(String clientId,
      String apiKey) {
    try {
      OzonWarehouseListResponse response = ozonRestClient.post()
          .uri(OZON_WAREHOUSE_LIST_URI)
          .header(HEADER_CLIENT_ID, clientId)
          .header(HEADER_API_KEY, apiKey)
          .body(new OzonWarehouseListRequest(BATCH_SIZE))
          .retrieve()
          .body(OzonWarehouseListResponse.class);

      if (response != null && response.getWarehouses() != null) {
        return response.getWarehouses();
      }
    } catch (RestClientException e) {
      log.error("Failed to fetch warehouse list from Ozon: {}", e.getMessage());
    }
    return List.of();
  }

  @Override
  public List<WarehouseAvailabilityModel> getWarehouseAvailability(UUID organizationId,
      Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);

    OzonApiKeyModel apiKey = userService.getOzonApiKeyByOrganizationId(organizationId);
    if (apiKey == null || !apiKey.isHasIntegration()) {
      log.info("Ozon integration is not configured for organization {}", organizationId);
      return List.of();
    }

    List<OzonAvailableWarehousesResponse.WarehouseAvailabilityItem> items =
        fetchAvailableWarehouses(apiKey.getOzonClientId(), apiKey.getOzonApiKey());

    return items.stream()
        .filter(item -> item.getWarehouse() != null && item.getSchedule() != null
            && item.getSchedule().getCapacity() != null
            && !item.getSchedule().getCapacity().isEmpty())
        .map(item -> {
          double avg = item.getSchedule().getCapacity().stream()
              .mapToLong(OzonAvailableWarehousesResponse.CapacityEntry::getValue)
              .average()
              .orElse(0.0);
          return new WarehouseAvailabilityModel(
              item.getWarehouse().getId(), item.getWarehouse().getName(), avg);
        })
        .toList();
  }

  private List<OzonAvailableWarehousesResponse.WarehouseAvailabilityItem> fetchAvailableWarehouses(
      String clientId, String apiKey) {
    try {
      OzonAvailableWarehousesResponse response = ozonRestClient.get()
          .uri(OZON_AVAILABLE_WAREHOUSES_URI)
          .header(HEADER_CLIENT_ID, clientId)
          .header(HEADER_API_KEY, apiKey)
          .retrieve()
          .body(OzonAvailableWarehousesResponse.class);

      if (response != null && response.getResult() != null) {
        return response.getResult();
      }
    } catch (RestClientException e) {
      log.error("Failed to fetch available warehouses from Ozon: {}", e.getMessage());
    }
    return List.of();
  }

  private void doSync(UUID organizationId, OzonApiKeyModel apiKey) {
    if (apiKey == null || !apiKey.isHasIntegration()) {
      return;
    }

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
    } while (!lastId.isBlank());

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

  private record PostingProduct(Long sku, int quantity, BigDecimal price, String currencyCode) {}
}
