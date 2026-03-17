package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kz.logisto.lgwarehouseservice.data.dto.itemvariant.CreateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.dto.itemvariant.UpdateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantModel;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantPointOfStorageModel;
import kz.logisto.lgwarehouseservice.service.ItemVariantPointOfStorageService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items/variants")
@Tag(name = "Items Variants controller")
public class ItemVariantController {

  private final ItemVariantService itemVariantService;
  private final ItemVariantPointOfStorageService itemVariantPointOfStorageService;

  @GetMapping("/{id}/counts")
  public ResponseEntity<List<ItemVariantPointOfStorageModel>> getCounts(@PathVariable UUID id,
      Principal principal) {
    return ResponseEntity.ok(
        itemVariantPointOfStorageService.getCountsByItemVariantId(id, principal));
  }

  @PostMapping
  public ResponseEntity<ItemVariantModel> createVariant(
      @Valid @RequestBody CreateItemVariantDto dto, Principal principal) {
    return ResponseEntity.ok(itemVariantService.create(dto, principal));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ItemVariantModel> updateVariant(@PathVariable UUID id,
      @Valid @RequestBody UpdateItemVariantDto dto, Principal principal) {
    return ResponseEntity.ok(itemVariantService.update(id, dto, principal));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteVariant(@PathVariable UUID id, Principal principal) {
    itemVariantService.delete(id, principal);
    return ResponseEntity.noContent().build();
  }
}
