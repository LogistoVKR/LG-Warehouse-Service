package kz.logisto.lgwarehouseservice.service;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.model.OzonSubscriptionModel;
import kz.logisto.lgwarehouseservice.data.model.WarehouseAvailabilityModel;

public interface OzonService {

  void syncProducts(UUID organizationId, Principal principal);

  void syncAllProducts();

  void syncWarehouses(UUID organizationId, Principal principal);

  void syncAllWarehouses();

  List<OzonSubscriptionModel> getSubscriptions(UUID organizationId, Principal principal);

  List<WarehouseAvailabilityModel> getWarehouseAvailability(UUID organizationId, Principal principal);
}

