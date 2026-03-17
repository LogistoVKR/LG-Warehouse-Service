package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.CreateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.ItemVariantMovementFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.UpdateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantMovementModel;
import kz.logisto.lgwarehouseservice.service.ItemVariantMovementService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items/movements")
@Tag(name = "Items Movement controller")
public class ItemVariantMovementController {

  private final ItemVariantMovementService itemVariantMovementService;

  @GetMapping
  public ResponseEntity<Page<ItemVariantMovementModel>> getAll(@RequestParam UUID organizationId,
      @ModelAttribute ItemVariantMovementFilterDto filter, @PageableDefault Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(itemVariantMovementService.getAllPageable(organizationId, filter, pageable, principal));
  }

  @PostMapping
  public ResponseEntity<ItemVariantMovementModel> createItemVariantMovement(
      @Valid @RequestBody CreateItemVariantMovementDto dto, Principal principal) {
    return ResponseEntity.ok(itemVariantMovementService.create(dto, principal));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ItemVariantMovementModel> updateItemVariantMovement(@PathVariable UUID id,
      @Valid @RequestBody UpdateItemVariantMovementDto dto, Principal principal) {
    return ResponseEntity.ok(itemVariantMovementService.update(id, dto, principal));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteItemVariantMovement(@PathVariable UUID id,
      Principal principal) {
    itemVariantMovementService.delete(id, principal);
    return ResponseEntity.noContent().build();
  }
}
