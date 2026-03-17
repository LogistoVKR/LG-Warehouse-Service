package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kz.logisto.lgwarehouseservice.data.dto.item.CreateItemDto;
import kz.logisto.lgwarehouseservice.data.dto.item.UpdateItemDto;
import kz.logisto.lgwarehouseservice.data.dto.item.ItemFilterDto;
import kz.logisto.lgwarehouseservice.data.model.ItemModel;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantModel;
import kz.logisto.lgwarehouseservice.service.ItemService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
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
@RequestMapping("/items")
@Tag(name = "Items controller")
public class ItemController {

  private final ItemService itemService;
  private final ItemVariantService itemVariantService;

  @GetMapping
  public ResponseEntity<Page<ItemModel>> getAll(@RequestParam UUID organizationId,
      @ModelAttribute ItemFilterDto filter, @PageableDefault Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(itemService.findAll(organizationId, filter, pageable, principal));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ItemModel> getById(@PathVariable UUID id, Principal principal) {
    return ResponseEntity.ok(itemService.findById(id, principal));
  }

  @PostMapping
  public ResponseEntity<ItemModel> create(@Valid @RequestBody CreateItemDto dto,
      Principal principal) {
    return ResponseEntity.ok(itemService.create(dto, principal));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ItemModel> update(@PathVariable UUID id,
      @Valid @RequestBody UpdateItemDto dto, Principal principal) {
    return ResponseEntity.ok(itemService.update(id, dto, principal));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
    itemService.delete(id, principal);
    return ResponseEntity.noContent()
        .build();
  }

  @GetMapping("/{id}/variants")
  public ResponseEntity<Page<ItemVariantModel>> getVariants(@PathVariable UUID id,
      @PageableDefault Pageable pageable, Principal principal) {
    return ResponseEntity.ok(itemVariantService.findByItemId(id, pageable, principal));
  }
}
