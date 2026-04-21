package kz.logisto.lgwarehouseservice.service;

import kz.logisto.lgwarehouseservice.data.dto.itemmovement.ItemVariantMovementFilterDto;
import kz.logisto.lgwarehouseservice.data.model.MovementAnalyticsModel;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface MovementAnalyticsService {

  MovementAnalyticsModel analyze(UUID organizationId, ItemVariantMovementFilterDto filter,
      Pageable pageable, Principal principal);
}
