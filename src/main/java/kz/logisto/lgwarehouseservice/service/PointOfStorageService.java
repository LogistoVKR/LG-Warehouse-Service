package kz.logisto.lgwarehouseservice.service;

import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.CreatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.PointOfStorageFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.UpdatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.model.PointOfStorageModel;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointOfStorageService {

  Page<PointOfStorageModel> findAll(UUID organizationId, PointOfStorageFilterDto filter,
      Pageable pageable, Principal principal);

  PointOfStorageModel findById(UUID id, Principal principal);

  PointOfStorageModel create(CreatePointOfStorageDto dto, Principal principal);

  PointOfStorageModel update(UUID id, UpdatePointOfStorageDto dto, Principal principal);

  void delete(UUID id, Principal principal);

  PointOfStorage getOrThrow(UUID id) throws NotFoundException;

  int countByOrganizationId(UUID organizationId);
}
