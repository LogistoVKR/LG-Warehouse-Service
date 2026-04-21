package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.ItemVariantMovementFilterDto;
import kz.logisto.lgwarehouseservice.data.model.MovementAnalyticsModel;
import kz.logisto.lgwarehouseservice.service.MovementAnalyticsService;
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

  @GetMapping("/movements")
  public ResponseEntity<MovementAnalyticsModel> analyzeMovements(
      @RequestParam UUID organizationId,
      @ModelAttribute ItemVariantMovementFilterDto filter,
      @PageableDefault Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(movementAnalyticsService.analyze(organizationId, filter, pageable, principal));
  }
}
