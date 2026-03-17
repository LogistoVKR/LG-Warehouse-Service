package kz.logisto.lgwarehouseservice.service;

import kz.logisto.lgwarehouseservice.data.dto.itemmovement.CreateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.ItemVariantMovementFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.UpdateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantMovement;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantMovementModel;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemVariantMovementService {

  Page<ItemVariantMovementModel> getAllPageable(UUID organizationId,
      ItemVariantMovementFilterDto filter, Pageable pageable, Principal principal);

  ItemVariantMovementModel create(CreateItemVariantMovementDto dto, Principal principal);

  ItemVariantMovementModel update(UUID id, UpdateItemVariantMovementDto dto, Principal principal);

  void delete(UUID id, Principal principal);

  ItemVariantMovement getOrThrow(UUID id) throws NotFoundException;
}
