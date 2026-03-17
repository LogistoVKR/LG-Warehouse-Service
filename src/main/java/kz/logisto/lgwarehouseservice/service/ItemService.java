package kz.logisto.lgwarehouseservice.service;

import kz.logisto.lgwarehouseservice.data.dto.item.CreateItemDto;
import kz.logisto.lgwarehouseservice.data.dto.item.ItemFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.item.UpdateItemDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.model.ItemModel;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {

  Page<ItemModel> findAll(UUID organizationId, ItemFilterDto filter, Pageable pageable,
      Principal principal);

  ItemModel findById(UUID id, Principal principal);

  ItemModel create(CreateItemDto dto, Principal principal);

  ItemModel update(UUID id, UpdateItemDto dto, Principal principal);

  void delete(UUID id, Principal principal);

  Item getOrThrow(UUID id) throws NotFoundException;
}
