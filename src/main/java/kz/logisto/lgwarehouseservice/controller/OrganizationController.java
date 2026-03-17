package kz.logisto.lgwarehouseservice.controller;

import kz.logisto.lgwarehouseservice.data.model.CountItemVariantPointOfStorageModel;
import kz.logisto.lgwarehouseservice.service.OrganizationService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/organizations")
public class OrganizationController {

  private final OrganizationService organizationService;

  @GetMapping("/{id}/counts")
  public ResponseEntity<CountItemVariantPointOfStorageModel> countItemVariantPointOfStorage(
      @PathVariable UUID id, Principal principal) {
    return ResponseEntity.ok(
        organizationService.countItemVariantsAndPointsOfStorage(id, principal));
  }
}
