package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.CreatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.PointOfStorageFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.UpdatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.model.PointOfStorageModel;
import kz.logisto.lgwarehouseservice.service.PointOfStorageService;
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
@RequestMapping("/points-of-storage")
@Tag(name = "Points of storage controller")
public class PointOfStorageController {

  private final PointOfStorageService pointOfStorageService;

  @GetMapping
  public ResponseEntity<Page<PointOfStorageModel>> getAll(@RequestParam UUID organizationId,
      @ModelAttribute PointOfStorageFilterDto filter, @PageableDefault Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(
        pointOfStorageService.findAll(organizationId, filter, pageable, principal));
  }

  @GetMapping("/{id}")
  public ResponseEntity<PointOfStorageModel> getById(@PathVariable UUID id, Principal principal) {
    return ResponseEntity.ok(pointOfStorageService.findById(id, principal));
  }

  @PostMapping
  public ResponseEntity<PointOfStorageModel> create(@Valid @RequestBody CreatePointOfStorageDto dto,
      Principal principal) {
    return ResponseEntity.ok(pointOfStorageService.create(dto, principal));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PointOfStorageModel> update(@PathVariable UUID id,
      @Valid @RequestBody UpdatePointOfStorageDto dto, Principal principal) {
    return ResponseEntity.ok(pointOfStorageService.update(id, dto, principal));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
    pointOfStorageService.delete(id, principal);
    return ResponseEntity.noContent().build();
  }
}
