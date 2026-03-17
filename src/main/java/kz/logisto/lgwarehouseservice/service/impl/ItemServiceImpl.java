package kz.logisto.lgwarehouseservice.service.impl;

import kz.logisto.lgwarehouseservice.data.dto.item.CreateItemDto;
import kz.logisto.lgwarehouseservice.data.dto.item.ItemFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.item.UpdateItemDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.model.ItemModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.mapper.ItemMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemService;
import jakarta.persistence.criteria.Predicate;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final ItemMapper itemMapper;
  private final AccessService accessService;
  private final ItemRepository itemRepository;

  @Override
  public Page<ItemModel> findAll(UUID organizationId, ItemFilterDto filter, Pageable pageable,
      Principal principal) {
    accessService.isMemberOrThrow(principal.getName(), organizationId);
    Specification<Item> specification = buildSpecification(organizationId, filter);
    return itemRepository.findAll(specification, pageable)
        .map(itemMapper::toModel);
  }

  @Override
  public ItemModel findById(UUID id, Principal principal) {
    Item item = getOrThrow(id);
    accessService.isMemberOrThrow(principal.getName(), item.getOrganizationId());
    return itemMapper.toModel(item);
  }

  @Override
  public ItemModel create(CreateItemDto dto, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), dto.organizationId());
    Item item = itemMapper.toEntity(dto);
    return itemMapper.toModel(itemRepository.save(item));
  }

  @Override
  public ItemModel update(UUID id, UpdateItemDto dto, Principal principal) {
    Item item = getOrThrow(id);
    accessService.canManageWarehouseOrThrow(principal.getName(), item.getOrganizationId());

    itemMapper.updateEntity(item, dto);
    return itemMapper.toModel(itemRepository.save(item));
  }

  @Override
  public void delete(UUID id, Principal principal) {
    Item item = getOrThrow(id);
    accessService.canManageWarehouseOrThrow(principal.getName(), item.getOrganizationId());
    itemRepository.delete(item);
  }

  @Override
  public Item getOrThrow(UUID id) throws NotFoundException {
    return itemRepository.findById(id)
        .orElseThrow(NotFoundException::new);
  }

  private Specification<Item> buildSpecification(UUID organizationId, ItemFilterDto filter) {
    return (root, query, cb) -> {
      List<Predicate> andPredicates = new ArrayList<>();
      List<Predicate> orPredicates = new ArrayList<>();

      andPredicates.add(cb.equal(root.get("organizationId"), organizationId));

      if (filter.name() != null) {
        if (Boolean.TRUE.equals(filter.or())) {
          orPredicates.add(cb.like(cb.lower(root.get("name")), filter.name()
              .toLowerCase() + "%"));
        } else {
          andPredicates.add(cb.like(cb.lower(root.get("name")), filter.name()
              .toLowerCase() + "%"));
        }
      }

      if (filter.sku() != null) {
        if (Boolean.TRUE.equals(filter.or())) {
          orPredicates.add(cb.like(cb.lower(root.get("variants")
              .get("sku")), filter.sku()
              .toLowerCase() + "%"));
        } else {
          andPredicates.add(cb.like(cb.lower(root.get("variants")
              .get("sku")), filter.sku()
              .toLowerCase() + "%"));
        }
      }

      if (filter.barcode() != null) {
        if (Boolean.TRUE.equals(filter.or())) {
          orPredicates.add(cb.like(cb.lower(root.get("variants")
              .get("barcode")), filter.barcode()
              .toLowerCase() + "%"));
        } else {
          andPredicates.add(cb.like(cb.lower(root.get("variants")
              .get("barcode")), filter.barcode()
              .toLowerCase() + "%"));
        }
      }

      Predicate andPart = cb.and(andPredicates.toArray(new Predicate[0]));
      Predicate orPart =
          orPredicates.isEmpty() ? cb.conjunction() : cb.or(orPredicates.toArray(new Predicate[0]));

      return cb.and(andPart, orPart);
    };
  }
}
