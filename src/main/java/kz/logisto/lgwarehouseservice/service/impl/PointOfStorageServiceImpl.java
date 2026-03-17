package kz.logisto.lgwarehouseservice.service.impl;

import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.CreatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.PointOfStorageFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.UpdatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.model.PointOfStorageModel;
import kz.logisto.lgwarehouseservice.data.repository.PointOfStorageRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.mapper.PointOfStorageMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.PointOfStorageService;
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
public class PointOfStorageServiceImpl implements PointOfStorageService {

  private final AccessService accessService;
  private final PointOfStorageMapper pointOfStorageMapper;
  private final PointOfStorageRepository pointOfStorageRepository;

  @Override
  public Page<PointOfStorageModel> findAll(UUID organizationId, PointOfStorageFilterDto filter,
      Pageable pageable, Principal principal) {
    accessService.isMemberOrThrow(principal.getName(), organizationId);
    Specification<PointOfStorage> specification = buildSpecification(organizationId, filter);
    return pointOfStorageRepository.findAll(specification, pageable)
        .map(pointOfStorageMapper::toModel);
  }

  @Override
  public PointOfStorageModel findById(UUID id, Principal principal) {
    PointOfStorage pointOfStorage = getOrThrow(id);
    accessService.isMemberOrThrow(principal.getName(), pointOfStorage.getOrganizationId());

    return pointOfStorageMapper.toModel(pointOfStorage);
  }

  @Override
  public PointOfStorageModel create(CreatePointOfStorageDto dto, Principal principal) {
    accessService.canManageWarehouseOrThrow(principal.getName(), dto.organizationId());

    PointOfStorage pointOfStorage = pointOfStorageMapper.toEntity(dto);
    return pointOfStorageMapper.toModel(pointOfStorageRepository.save(pointOfStorage));
  }

  @Override
  public PointOfStorageModel update(UUID id, UpdatePointOfStorageDto dto, Principal principal) {
    PointOfStorage pointOfStorage = getOrThrow(id);

    accessService.canManageWarehouseOrThrow(principal.getName(),
        pointOfStorage.getOrganizationId());

    pointOfStorageMapper.updateEntity(pointOfStorage, dto);
    return pointOfStorageMapper.toModel(pointOfStorageRepository.save(pointOfStorage));
  }

  @Override
  public void delete(UUID id, Principal principal) {
    PointOfStorage pointOfStorage = getOrThrow(id);

    accessService.canManageWarehouseOrThrow(principal.getName(),
        pointOfStorage.getOrganizationId());

    pointOfStorageRepository.delete(pointOfStorage);
  }

  @Override
  public PointOfStorage getOrThrow(UUID id) throws NotFoundException {
    return pointOfStorageRepository.findById(id)
        .orElseThrow(NotFoundException::new);
  }

  @Override
  public int countByOrganizationId(UUID organizationId) {
    return pointOfStorageRepository.countByOrganizationId(organizationId);
  }

  private Specification<PointOfStorage> buildSpecification(UUID organizationId,
      PointOfStorageFilterDto filter) {
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

      if (filter.location() != null) {
        if (Boolean.TRUE.equals(filter.or())) {
          orPredicates.add(cb.like(cb.lower(root.get("location")), filter.location()
              .toLowerCase() + "%"));
        } else {
          andPredicates.add(cb.like(cb.lower(root.get("location")), filter.location()
              .toLowerCase() + "%"));
        }
      }

      if (filter.type() != null) {
        if (Boolean.TRUE.equals(filter.or())) {
          orPredicates.add(cb.equal(root.get("type"), filter.type()));
        } else {
          andPredicates.add(cb.equal(root.get("type"), filter.type()));
        }
      }

      Predicate andPart = cb.and(andPredicates.toArray(new Predicate[0]));
      Predicate orPart =
          orPredicates.isEmpty() ? cb.conjunction() : cb.or(orPredicates.toArray(new Predicate[0]));

      return cb.and(andPart, orPart);
    };
  }
}
