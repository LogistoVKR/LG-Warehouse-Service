package kz.logisto.lgwarehouseservice.service;

import kz.logisto.lgwarehouseservice.data.model.CountItemVariantPointOfStorageModel;
import java.security.Principal;
import java.util.UUID;

public interface OrganizationService {

  CountItemVariantPointOfStorageModel countItemVariantsAndPointsOfStorage(UUID organizationId,
      Principal principal);
}
