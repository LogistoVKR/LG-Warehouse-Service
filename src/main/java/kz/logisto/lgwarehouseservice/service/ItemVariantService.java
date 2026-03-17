package kz.logisto.lgwarehouseservice.service;

import kz.logisto.lgwarehouseservice.data.dto.itemvariant.CreateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.dto.itemvariant.UpdateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantModel;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemVariantService {

  Page<ItemVariantModel> findByItemId(UUID itemId, Pageable pageable, Principal principal);

  ItemVariantModel create(CreateItemVariantDto dto, Principal principal);

  ItemVariantModel update(UUID id, UpdateItemVariantDto dto, Principal principal);

  void delete(UUID id, Principal principal);

  ItemVariant getOrThrow(UUID id) throws NotFoundException;

  int countByOrganizationId(UUID organizationId);
}
