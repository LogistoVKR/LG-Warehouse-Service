package kz.logisto.lgwarehouseservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.ItemVariantMovementFilterDto;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantPointOfStorage;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantMovementModel;
import kz.logisto.lgwarehouseservice.data.model.MovementAnalyticsModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantPointOfStorageRepository;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemVariantMovementService;
import kz.logisto.lgwarehouseservice.service.MovementAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovementAnalyticsServiceImpl implements MovementAnalyticsService {

  private static final String SYSTEM_PROMPT = """
      Ты — эксперт по складской аналитике. Проанализируй предоставленную историю перемещений \
      и текущие остатки на складах.

      Определи:
      1. Товары с риском дефицита — остатки снижаются, продажи и списания опережают закупки
      2. Товары с профицитом — избыточные запасы относительно темпов расхода
      3. Конкретные рекомендации по перемещениям — трансферы между складами, предложения по закупкам, списания

      Основывай анализ на скорости движения товаров, текущих остатках, зарезервированных количествах \
      и тенденциях в данных. Указывай конкретные SKU и названия складов.

      Отвечай ТОЛЬКО на русском языке. Формат ответа — валидный JSON по запрошенной схеме.""";

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;
  private final AccessService accessService;
  private final ItemVariantMovementService movementService;
  private final ItemVariantPointOfStorageRepository stockRepository;
  private final OpenAiChatOptions analyticsResponseOptions;
  private final BeanOutputConverter<MovementAnalyticsModel> movementAnalyticsConverter;

  @Override
  public MovementAnalyticsModel analyze(UUID organizationId, ItemVariantMovementFilterDto filter,
      Pageable pageable, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);

    Page<ItemVariantMovementModel> movements = movementService
        .getAllPageable(organizationId, filter, pageable, principal);

    Set<UUID> variantIds = movements.getContent().stream()
        .map(m -> m.getItemVariant().getId())
        .collect(Collectors.toSet());

    List<ItemVariantPointOfStorage> stockLevels = variantIds.isEmpty()
        ? List.of()
        : stockRepository.findByIdItemVariantIdIn(variantIds);

    String userPrompt = buildUserPrompt(movements.getContent(), stockLevels);

    String response = chatClient.prompt()
        .system(SYSTEM_PROMPT)
        .user(userPrompt)
        .options(analyticsResponseOptions)
        .call()
        .content();

    if (response == null) {
      return new MovementAnalyticsModel();
    }

    return movementAnalyticsConverter.convert(response);
  }

  private String buildUserPrompt(List<ItemVariantMovementModel> movements,
      List<ItemVariantPointOfStorage> stockLevels) {
    List<Map<String, Object>> movementData = movements.stream()
        .map(m -> {
          Map<String, Object> entry = new LinkedHashMap<>();
          entry.put("type", m.getType());
          entry.put("itemVariantSku",
              m.getItemVariant() != null ? m.getItemVariant().getSku() : null);
          entry.put("fromLocation",
              m.getFromPointOfStorage() != null ? m.getFromPointOfStorage().getName() : null);
          entry.put("toLocation",
              m.getToPointOfStorage() != null ? m.getToPointOfStorage().getName() : null);
          entry.put("quantity", m.getQuantity());
          entry.put("pricePerItem", m.getPricePerItem());
          entry.put("currency", m.getCurrency());
          entry.put("date", m.getCreated());
          return entry;
        })
        .toList();

    List<Map<String, Object>> stockData = stockLevels.stream()
        .map(s -> {
          Map<String, Object> entry = new LinkedHashMap<>();
          entry.put("itemVariantId", s.getId().getItemVariantId());
          entry.put("pointOfStorageName",
              s.getPointOfStorage() != null ? s.getPointOfStorage().getName() : null);
          entry.put("pointOfStorageType",
              s.getPointOfStorage() != null ? s.getPointOfStorage().getType() : null);
          entry.put("currentQuantity", s.getQuantity());
          entry.put("reserved", s.getReserved());
          return entry;
        })
        .toList();

    try {
      String movementsJson = objectMapper.writeValueAsString(movementData);
      String stockJson = objectMapper.writeValueAsString(stockData);
      return "Movement history:\n" + movementsJson + "\n\nCurrent stock levels:\n" + stockJson;
    } catch (JsonProcessingException e) {
      return "Movement history:\n" + movementData + "\n\nCurrent stock levels:\n" + stockData;
    }
  }
}
