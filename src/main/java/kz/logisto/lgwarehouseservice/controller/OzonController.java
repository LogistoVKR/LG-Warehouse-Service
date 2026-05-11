package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.model.WarehouseAvailabilityModel;
import kz.logisto.lgwarehouseservice.service.OzonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ozon")
@Tag(name = "Ozon controller")
public class OzonController {

  private final OzonService ozonService;

  @PostMapping("/sync/items")
  public ResponseEntity<Void> sync(@RequestParam UUID organizationId, Principal principal) {
    ozonService.syncProducts(organizationId, principal);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/sync/warehouses")
  public ResponseEntity<Void> syncWarehouses(@RequestParam UUID organizationId,
      Principal principal) {
    ozonService.syncWarehouses(organizationId, principal);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/sync/stocks")
  public ResponseEntity<Void> syncStocks(@RequestParam UUID organizationId, Principal principal) {
    ozonService.syncStocksToOzon(organizationId, principal);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/pull/postings")
  public ResponseEntity<Void> pullPostings() {
    ozonService.pullAllPostings();
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reconcile")
  public ResponseEntity<Void> reconcile() {
    ozonService.reconcileAllStocks();
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/warehouses/availability")
  public ResponseEntity<List<WarehouseAvailabilityModel>> getWarehouseAvailability(
      @RequestParam UUID organizationId, Principal principal) {
    return ResponseEntity.ok(ozonService.getWarehouseAvailability(organizationId, principal));
  }
}
