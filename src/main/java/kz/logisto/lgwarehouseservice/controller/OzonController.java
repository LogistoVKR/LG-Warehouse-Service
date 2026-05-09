package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.service.OzonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

  @PostMapping("/sync")
  public ResponseEntity<Void> sync(@RequestParam UUID organizationId, Principal principal) {
    ozonService.syncProducts(organizationId, principal);
    return ResponseEntity.noContent().build();
  }
}
