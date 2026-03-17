package kz.logisto.lgwarehouseservice.service;

import kz.logisto.lgwarehouseservice.data.dto.itemvariantpointofstorage.ItemVariantPointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantPointOfStorageModel;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

public interface ItemVariantPointOfStorageService {

  List<ItemVariantPointOfStorageModel> getCountsByItemVariantId(UUID id, Principal principal);

  void update(ItemVariantPointOfStorageDto dto);

  void undoUpdate(ItemVariantPointOfStorageDto dto);
}
