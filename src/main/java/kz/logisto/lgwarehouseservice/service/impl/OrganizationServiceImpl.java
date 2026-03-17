package kz.logisto.lgwarehouseservice.service.impl;

import kz.logisto.lgwarehouseservice.data.model.CountItemVariantPointOfStorageModel;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import kz.logisto.lgwarehouseservice.service.OrganizationService;
import kz.logisto.lgwarehouseservice.service.PointOfStorageService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

  private final AccessService accessService;
  private final ItemVariantService itemVariantService;
  private final PointOfStorageService pointOfStorageService;

  @Override
  public CountItemVariantPointOfStorageModel countItemVariantsAndPointsOfStorage(
      UUID organizationId, Principal principal) {
    accessService.isMemberOrThrow(principal.getName(), organizationId);
    int itemVariants = itemVariantService.countByOrganizationId(organizationId);
    int pointsOfStorage = pointOfStorageService.countByOrganizationId(organizationId);
    return new CountItemVariantPointOfStorageModel(itemVariants, pointsOfStorage);
  }
}
