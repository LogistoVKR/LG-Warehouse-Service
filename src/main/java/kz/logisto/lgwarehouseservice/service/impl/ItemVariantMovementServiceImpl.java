package kz.logisto.lgwarehouseservice.service.impl;

import kz.logisto.lgwarehouseservice.data.dto.itemmovement.CreateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.ItemVariantMovementFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.UpdateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.dto.itemvariantpointofstorage.ItemVariantPointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantMovement;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantMovementModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantMovementRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantMovementMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemVariantMovementService;
import kz.logisto.lgwarehouseservice.service.ItemVariantPointOfStorageService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import kz.logisto.lgwarehouseservice.service.PointOfStorageService;
import kz.logisto.lgwarehouseservice.util.ListUtils;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class ItemVariantMovementServiceImpl implements ItemVariantMovementService {

  private final AccessService accessService;
  private final ItemVariantMovementMapper mapper;
  private final ItemVariantService itemVariantService;
  private final ItemVariantMovementRepository repository;
  private final PointOfStorageService pointOfStorageService;
  private final ItemVariantPointOfStorageService itemVariantPointOfStorageService;

  @Override
  public Page<ItemVariantMovementModel> getAllPageable(UUID organizationId,
      ItemVariantMovementFilterDto filter, Pageable pageable, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), organizationId);
    Specification<ItemVariantMovement> specification = buildSpecification(organizationId, filter);
    return repository.findAll(specification, pageable).map(mapper::toModel);
  }

  @Override
  @Transactional
  public ItemVariantMovementModel create(CreateItemVariantMovementDto dto, Principal principal) {
    ItemVariantMovement movement = mapper.toEntity(dto);
    accessService.canManageWarehouseOrThrow(principal.getName(), dto.organizationId());

    updateMovementRelations(dto.fromPointOfStorageId(), dto.toPointOfStorageId(),
        dto.itemVariantId(), movement, principal);

    itemVariantPointOfStorageService.update(
        new ItemVariantPointOfStorageDto(movement.getType(), movement.getFromPointOfStorageId(),
            movement.getToPointOfStorageId(), movement.getItemVariantId(), movement.getQuantity()));

    return mapper.toModel(repository.save(movement));
  }

  @Override
  @Transactional
  public ItemVariantMovementModel update(UUID id, UpdateItemVariantMovementDto dto,
      Principal principal) {
    ItemVariantMovement movement = getOrThrow(id);
    accessService.canManageWarehouseOrThrow(principal.getName(), movement.getOrganizationId());

    itemVariantPointOfStorageService.undoUpdate(
        new ItemVariantPointOfStorageDto(movement.getType(), movement.getFromPointOfStorageId(),
            movement.getToPointOfStorageId(), movement.getItemVariantId(), movement.getQuantity()));

    mapper.updateEntity(movement, dto);
    updateMovementRelations(dto.fromPointOfStorageId(), dto.toPointOfStorageId(),
        dto.itemVariantId(), movement, principal);

    itemVariantPointOfStorageService.update(
        new ItemVariantPointOfStorageDto(movement.getType(), movement.getFromPointOfStorageId(),
            movement.getToPointOfStorageId(), movement.getItemVariantId(), dto.quantity()));

    return mapper.toModel(repository.save(movement));
  }

  @Override
  @Transactional
  public void delete(UUID id, Principal principal) {
    ItemVariantMovement movement = getOrThrow(id);
    accessService.canManageWarehouseOrThrow(principal.getName(), movement.getOrganizationId());
    itemVariantPointOfStorageService.undoUpdate(
        new ItemVariantPointOfStorageDto(movement.getType(), movement.getFromPointOfStorageId(),
            movement.getToPointOfStorageId(), movement.getItemVariantId(), movement.getQuantity()));
    repository.delete(movement);
  }

  @Override
  public ItemVariantMovement getOrThrow(UUID id) throws NotFoundException {
    return repository.findById(id).orElseThrow(NotFoundException::new);
  }

  private Specification<ItemVariantMovement> buildSpecification(UUID organizationId,
      ItemVariantMovementFilterDto filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.equal(root.get("organizationId"), organizationId));

      if (filter.fromPointOfStorageId() != null) {
        predicates.add(
            cb.equal(root.get("fromPointOfStorage").get("id"), filter.fromPointOfStorageId()));
      }

      if (filter.toPointOfStorageId() != null) {
        predicates.add(
            cb.equal(root.get("toPointOfStorage").get("id"), filter.toPointOfStorageId()));
      }

      if (filter.itemVariantId() != null) {
        predicates.add(cb.equal(root.get("itemVariant").get("id"), filter.itemVariantId()));
      }

      if (filter.type() != null) {
        predicates.add(cb.equal(root.get("type"), filter.type()));
      }

      if (filter.from() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("created"), filter.from()));
      }

      if (filter.to() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("created"), filter.to()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private void updateMovementRelations(UUID fromPointOfStorageId, UUID toPointOfStorageId,
      UUID itemVariantId, ItemVariantMovement movement, Principal principal) {
    List<UUID> organizationIds = new ArrayList<>(3);
    if (fromPointOfStorageId != null) {
      PointOfStorage pointOfStorage = pointOfStorageService.getOrThrow(fromPointOfStorageId);
      movement.setFromPointOfStorage(pointOfStorage);
      organizationIds.add(pointOfStorage.getOrganizationId());
    }

    if (toPointOfStorageId != null) {
      PointOfStorage pointOfStorage = pointOfStorageService.getOrThrow(toPointOfStorageId);
      movement.setToPointOfStorage(pointOfStorage);
      organizationIds.add(pointOfStorage.getOrganizationId());
    }

    if (itemVariantId != null) {
      ItemVariant itemVariant = itemVariantService.getOrThrow(itemVariantId);
      movement.setItemVariant(itemVariant);
      organizationIds.add(itemVariant.getOrganizationId());
    }

    if (CollectionUtils.isEmpty(organizationIds)) {
      return;
    }

    accessService.canManageWarehouseOrThrow(principal.getName(),
        ListUtils.getLastInEquals(organizationIds));
  }
}
