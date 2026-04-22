package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.CreatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.PointOfStorageFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.UpdatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.enums.PointOfStorageType;
import kz.logisto.lgwarehouseservice.data.model.PointOfStorageModel;
import kz.logisto.lgwarehouseservice.data.repository.PointOfStorageRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.mapper.PointOfStorageMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.PointOfStorageService;
import kz.logisto.lgwarehouseservice.service.impl.PointOfStorageServiceImpl;
import kz.logisto.lgwarehouseservice.util.TestPrincipalFactory;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class PointOfStorageTest {

  @Mock
  private AccessService accessService;

  @Mock
  private PointOfStorageMapper mapper;

  @Mock
  private PointOfStorageRepository repository;

  private PointOfStorageService service;

  @BeforeEach
  void init() {
    service = new PointOfStorageServiceImpl(accessService, mapper, repository);
  }

  @Test
  void findById_valid_returnsModel() {
    Principal principal = TestPrincipalFactory.create();
    PointOfStorage entity = new PointOfStorage();
    entity.setId(UUID.randomUUID());
    entity.setOrganizationId(UUID.randomUUID());
    PointOfStorageModel model = new PointOfStorageModel();

    when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
    when(mapper.toModel(entity)).thenReturn(model);

    PointOfStorageModel result = assertDoesNotThrow(
        () -> service.findById(entity.getId(), principal));

    assertNotNull(result);
    verify(accessService).isMemberOrThrow(principal.getName(), entity.getOrganizationId());
    verify(repository).findById(entity.getId());
    verify(mapper).toModel(entity);
  }

  @Test
  void findById_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.findById(id, principal));
  }

  @Test
  void findById_accessDenied_throws() {
    Principal principal = TestPrincipalFactory.create();
    PointOfStorage entity = new PointOfStorage();
    entity.setId(UUID.randomUUID());
    entity.setOrganizationId(UUID.randomUUID());

    when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
    doThrow(new NotFoundException()).when(accessService)
        .isMemberOrThrow(principal.getName(), entity.getOrganizationId());

    assertThrows(NotFoundException.class, () -> service.findById(entity.getId(), principal));
  }

  @Test
  @SuppressWarnings("unchecked")
  void findAll_valid_returnsPage() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);
    PointOfStorageFilterDto filter = new PointOfStorageFilterDto(null, null, null, null);
    PointOfStorage entity = new PointOfStorage();
    Page<PointOfStorage> page = new PageImpl<>(List.of(entity));
    PointOfStorageModel model = new PointOfStorageModel();

    when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(mapper.toModel(entity)).thenReturn(model);

    Page<PointOfStorageModel> result = service.findAll(orgId, filter, pageable, principal);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(accessService).isMemberOrThrow(principal.getName(), orgId);
  }

  @Test
  void create_valid_savesAndReturns() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    CreatePointOfStorageDto dto = new CreatePointOfStorageDto("warehouse", "desc", "loc", orgId,
        PointOfStorageType.WAREHOUSE);
    PointOfStorage entity = new PointOfStorage();
    PointOfStorage saved = new PointOfStorage();
    saved.setId(UUID.randomUUID());
    PointOfStorageModel model = new PointOfStorageModel();

    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(saved);
    when(mapper.toModel(saved)).thenReturn(model);

    PointOfStorageModel result = service.create(dto, principal);

    assertNotNull(result);
    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
  }

  @Test
  void create_accessDenied_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    CreatePointOfStorageDto dto = new CreatePointOfStorageDto("warehouse", null, null, orgId,
        PointOfStorageType.WAREHOUSE);
    doThrow(new NotFoundException()).when(accessService)
        .canManageWarehouseOrThrow(principal.getName(), orgId);

    assertThrows(NotFoundException.class, () -> service.create(dto, principal));
  }

  @Test
  void update_valid_updatesAndReturns() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UpdatePointOfStorageDto dto = new UpdatePointOfStorageDto("new name", null, null, null);
    PointOfStorage entity = new PointOfStorage();
    entity.setId(id);
    entity.setOrganizationId(orgId);
    PointOfStorageModel model = new PointOfStorageModel();

    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model);

    PointOfStorageModel result = service.update(id, dto, principal);

    assertNotNull(result);
    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(mapper).updateEntity(entity, dto);
  }

  @Test
  void update_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    UpdatePointOfStorageDto dto = new UpdatePointOfStorageDto("name", null, null, null);
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.update(id, dto, principal));
  }

  @Test
  void delete_valid_deletes() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    PointOfStorage entity = new PointOfStorage();
    entity.setId(id);
    entity.setOrganizationId(orgId);

    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.delete(id, principal);

    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(repository).delete(entity);
  }

  @Test
  void delete_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.delete(id, principal));
  }

  @Test
  void getOrThrow_found_returns() {
    UUID id = UUID.randomUUID();
    PointOfStorage entity = new PointOfStorage();
    entity.setId(id);
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    PointOfStorage result = service.getOrThrow(id);

    assertEquals(id, result.getId());
  }

  @Test
  void getOrThrow_notFound_throws() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getOrThrow(id));
  }

  @Test
  void countByOrganizationId_returnsCount() {
    UUID orgId = UUID.randomUUID();
    when(repository.countByOrganizationId(orgId)).thenReturn(5);

    int result = service.countByOrganizationId(orgId);

    assertEquals(5, result);
  }
}
