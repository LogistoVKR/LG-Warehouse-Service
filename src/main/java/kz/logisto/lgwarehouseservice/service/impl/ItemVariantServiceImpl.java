package kz.logisto.lgwarehouseservice.service.impl;

import kz.logisto.lgwarehouseservice.data.dto.itemvariant.CreateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.dto.itemvariant.UpdateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemVariantServiceImpl implements ItemVariantService {

  private final ItemService itemService;
  private final AccessService accessService;
  private final ItemVariantMapper itemVariantMapper;
  private final ItemVariantRepository itemVariantRepository;

  @Override
  public Page<ItemVariantModel> findByItemId(UUID itemId, Pageable pageable, Principal principal) {
    Item item = itemService.getOrThrow(itemId);
    accessService.isMemberOrThrow(principal.getName(), item.getOrganizationId());

    return itemVariantRepository.findAllByItemId(itemId, pageable)
        .map(itemVariantMapper::toModel);
  }

  @Override
  public ItemVariantModel create(CreateItemVariantDto dto, Principal principal) {
    Item item = itemService.getOrThrow(dto.itemId());
    accessService.canManageWarehouseOrThrow(principal.getName(), item.getOrganizationId());

    ItemVariant itemVariant = itemVariantMapper.toEntity(dto);
    itemVariant.setItem(item);
    return itemVariantMapper.toModel(itemVariantRepository.save(itemVariant));
  }

  @Override
  public ItemVariantModel update(UUID id, UpdateItemVariantDto dto, Principal principal) {
    ItemVariant itemVariant = getOrThrow(id);

    Item item = itemVariant.getItem();
    accessService.canManageWarehouseOrThrow(principal.getName(), item.getOrganizationId());

    itemVariantMapper.updateEntity(itemVariant, dto);
    return itemVariantMapper.toModel(itemVariantRepository.save(itemVariant));
  }

  @Override
  public void delete(UUID id, Principal principal) {
    ItemVariant itemVariant = getOrThrow(id);

    Item item = itemVariant.getItem();
    accessService.canManageWarehouseOrThrow(principal.getName(), item.getOrganizationId());

    itemVariantRepository.delete(itemVariant);
  }

  @Override
  public ItemVariant getOrThrow(UUID id) throws NotFoundException {
    return itemVariantRepository.findById(id)
        .orElseThrow(NotFoundException::new);
  }

  @Override
  public int countByOrganizationId(UUID organizationId) {
    return itemVariantRepository.countByItem_OrganizationId(organizationId);
  }
}
