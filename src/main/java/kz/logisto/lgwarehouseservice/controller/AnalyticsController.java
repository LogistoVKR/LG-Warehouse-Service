package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.ItemVariantMovementFilterDto;
import kz.logisto.lgwarehouseservice.data.model.MovementAnalyticsModel;
import kz.logisto.lgwarehouseservice.data.model.OzonSubscriptionModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.mapper.ItemMapper;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantMapper;
import kz.logisto.lgwarehouseservice.service.MovementAnalyticsService;
import kz.logisto.lgwarehouseservice.service.OzonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
@Tag(name = "Analytics controller")
public class AnalyticsController {

  private final MovementAnalyticsService movementAnalyticsService;
  private final OzonService ozonService;

  @GetMapping("/movements")
  public ResponseEntity<MovementAnalyticsModel> analyzeMovements(
      @RequestParam UUID organizationId,
      @ModelAttribute ItemVariantMovementFilterDto filter,
      @PageableDefault Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(
        movementAnalyticsService.analyze(organizationId, filter, pageable, principal));
  }

  private final ItemMapper itemMapper;
  private final ItemRepository itemRepository;
  private final ItemVariantMapper itemVariantMapper;

  @GetMapping("/subscriptions")
  public ResponseEntity<List<OzonSubscriptionModel>> getOzonSubscriptions(
      @RequestParam UUID organizationId,
      Principal principal) {
//    Item item = itemRepository.findAll().getFirst();
//    ItemVariant itemVariant = item.getVariants().getFirst();
//    return ResponseEntity.ok(List.of(new OzonSubscriptionModel(itemMapper.toModel(item),
//        List.of(new OzonSubscriptionVariantModel(itemVariantMapper.toModel(itemVariant), 1)))));
    return ResponseEntity.ok(ozonService.getSubscriptions(organizationId, principal));
  }
}
