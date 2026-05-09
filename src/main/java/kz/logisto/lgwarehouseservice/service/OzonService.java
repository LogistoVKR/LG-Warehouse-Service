package kz.logisto.lgwarehouseservice.service;

import java.security.Principal;
import java.util.UUID;

public interface OzonService {

  void syncProducts(UUID organizationId, Principal principal);
}

