package kz.logisto.lgwarehouseservice.service.impl;

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.logisto.lgwarehouseservice.data.dto.search.ItemSearchDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantPointOfStorage;
import kz.logisto.lgwarehouseservice.data.model.ItemSearchResponseModel;
import kz.logisto.lgwarehouseservice.data.model.ItemSearchResultModel;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantPointOfStorageModel;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantWithStorageModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantPointOfStorageRepository;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantPointOfStorageMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemSearchServiceImpl implements ItemSearchService {

  private final VectorStore vectorStore;
  private final AccessService accessService;
  private final ItemRepository itemRepository;
  private final ItemVariantPointOfStorageMapper stockMapper;
  private final ItemVariantPointOfStorageRepository stockRepository;

  @Override
  @Transactional(readOnly = true)
  public ItemSearchResponseModel search(ItemSearchDto dto) {
    SearchRequest.Builder builder = SearchRequest.builder()
        .query(dto.query())
        .topK(dto.topK())
        .similarityThreshold(0.3);

    SearchRequest request = builder.build();

    List<Document> results = vectorStore.similaritySearch(request);

    if (CollectionUtils.isEmpty(results)) {
      results = List.of();
    }

    Map<UUID, Double> scoreMap = results.stream()
        .collect(Collectors.toMap(
            doc -> UUID.fromString(doc.getMetadata().get("itemId").toString()),
            doc -> doc.getScore() != null ? doc.getScore() : 0.0,
            Math::max
        ));

    List<UUID> itemIds = results.stream()
        .map(doc -> UUID.fromString(doc.getMetadata().get("itemId").toString()))
        .distinct()
        .toList();

    List<Item> items = itemRepository.findAllById(itemIds);

    List<ItemSearchResultModel> resultModels = items.stream()
        .map(item -> buildSearchResult(item, scoreMap.getOrDefault(item.getId(), 0.0)))
        .sorted(Comparator.comparingDouble(ItemSearchResultModel::getScore).reversed())
        .toList();

    return new ItemSearchResponseModel(dto.query(), resultModels.size(), resultModels);
  }

  @Override
  public void indexItem(Item item) {
    removeItem(item.getId());

    Document document = new Document(
        item.getId().toString(),
        buildContent(item),
        buildMetadata(item)
    );

    vectorStore.add(List.of(document));
  }

  @Override
  public void indexAllItems(UUID organizationId, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);

    List<Item> items = itemRepository.findAllByOrganizationId(organizationId);

    List<Document> documents = items.stream()
        .map(item -> new Document(
            item.getId().toString(),
            buildContent(item),
            buildMetadata(item)
        ))
        .toList();

    int batchSize = 50;
    for (int i = 0; i < documents.size(); i += batchSize) {
      List<Document> batch = documents.subList(i, Math.min(i + batchSize, documents.size()));
      vectorStore.add(batch);
    }
  }

  @Override
  public void removeItem(UUID itemId) {
    vectorStore.delete(List.of(itemId.toString()));
  }

  private ItemSearchResultModel buildSearchResult(Item item, double score) {
    List<ItemVariantWithStorageModel> variantModels = item.getVariants().stream()
        .map(this::buildVariantWithStorage)
        .filter(v -> !v.getStorageLocations().isEmpty())
        .toList();

    ItemSearchResultModel result = new ItemSearchResultModel();
    result.setItemId(item.getId());
    result.setName(item.getName());
    result.setDescription(item.getDescription());
    result.setOrganizationId(item.getOrganizationId());
    result.setScore(score);
    result.setVariants(variantModels);
    return result;
  }

  private ItemVariantWithStorageModel buildVariantWithStorage(ItemVariant variant) {
    List<ItemVariantPointOfStorage> stock = stockRepository.findByItemVariantId(variant.getId());

    List<ItemVariantPointOfStorageModel> storageModels = stock.stream()
        .filter(s -> s.getQuantity() > 0)
        .map(stockMapper::toModel)
        .toList();

    ItemVariantWithStorageModel model = new ItemVariantWithStorageModel();
    model.setId(variant.getId());
    model.setSku(variant.getSku());
    model.setBarcode(variant.getBarcode());
    model.setPrice(variant.getPrice());
    model.setCurrency(variant.getCurrency());
    model.setStorageLocations(storageModels);
    return model;
  }

  private String buildContent(Item item) {
    StringBuilder sb = new StringBuilder();
    sb.append(item.getName());
    if (item.getDescription() != null && !item.getDescription().isBlank()) {
      sb.append(". ").append(item.getDescription());
    }
    return sb.toString();
  }

  private Map<String, Object> buildMetadata(Item item) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("itemId", item.getId().toString());
    metadata.put("organizationId", item.getOrganizationId().toString());
    return metadata;
  }
}
