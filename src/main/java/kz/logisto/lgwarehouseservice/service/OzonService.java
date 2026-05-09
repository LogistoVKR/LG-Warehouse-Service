package kz.logisto.lgwarehouseservice.service;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.model.OzonSubscriptionModel;

public interface OzonService {

  void syncProducts(UUID organizationId, Principal principal);

  void syncAllProducts();

  List<OzonSubscriptionModel> getSubscriptions(UUID organizationId, Principal principal);
}

