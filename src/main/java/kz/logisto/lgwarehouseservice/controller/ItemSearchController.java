package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.dto.search.ItemSearchDto;
import kz.logisto.lgwarehouseservice.data.model.ItemSearchResponseModel;
import kz.logisto.lgwarehouseservice.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
@Tag(name = "Item Search controller")
public class ItemSearchController {

  private final ItemSearchService itemSearchService;

  @GetMapping("/items")
  public ResponseEntity<ItemSearchResponseModel> searchItems(
      @Valid @ModelAttribute ItemSearchDto dto) {
    return ResponseEntity.ok(itemSearchService.search(dto));
  }

  @PostMapping("/items/index")
  public ResponseEntity<Void> indexAllItems(
      @RequestParam UUID organizationId,
      Principal principal) {
    itemSearchService.indexAllItems(organizationId, principal);
    return ResponseEntity.accepted().build();
  }
}
