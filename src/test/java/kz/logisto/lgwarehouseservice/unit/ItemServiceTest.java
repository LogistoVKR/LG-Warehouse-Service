package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kz.logisto.lgwarehouseservice.data.dto.item.CreateItemDto;
import kz.logisto.lgwarehouseservice.data.dto.item.ItemFilterDto;
import kz.logisto.lgwarehouseservice.data.dto.item.UpdateItemDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.model.ItemModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.mapper.ItemMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemSearchService;
import kz.logisto.lgwarehouseservice.service.ItemService;
import kz.logisto.lgwarehouseservice.service.impl.ItemServiceImpl;
import kz.logisto.lgwarehouseservice.util.TestPrincipalFactory;
import java.security.Principal;
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
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

  @Mock
  private ItemMapper itemMapper;

  @Mock
  private AccessService accessService;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private ItemSearchService itemSearchService;

  private ItemService service;

  @BeforeEach
  void init() {
    service = new ItemServiceImpl(itemMapper, accessService, itemRepository, itemSearchService);
  }

  @Test
  void findById_valid_returnsModel() {
    Principal principal = TestPrincipalFactory.create();
    Item item = new Item();
    item.setId(UUID.randomUUID());
    item.setOrganizationId(UUID.randomUUID());
    ItemModel model = new ItemModel(item.getId(), "name", "desc");

    when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    when(itemMapper.toModel(item)).thenReturn(model);

    ItemModel result = service.findById(item.getId(), principal);

    assertNotNull(result);
    assertEquals(model.getId(), result.getId());
    verify(accessService).isMemberOrThrow(principal.getName(), item.getOrganizationId());
  }

  @Test
  void findById_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    when(itemRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.findById(id, principal));
  }

  @Test
  @SuppressWarnings("unchecked")
  void findAll_valid_returnsPage() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);
    ItemFilterDto filter = new ItemFilterDto(null, null, null, null);
    Item item = new Item();
    item.setOrganizationId(orgId);
    Page<Item> itemPage = new PageImpl<>(List.of(item));
    ItemModel model = new ItemModel();

    when(itemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(itemPage);
    when(itemMapper.toModel(item)).thenReturn(model);

    Page<ItemModel> result = service.findAll(orgId, filter, pageable, principal);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(accessService).isMemberOrThrow(principal.getName(), orgId);
  }

  @Test
  void create_valid_savesAndIndexes() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    CreateItemDto dto = new CreateItemDto("item", "desc", orgId);
    Item entity = new Item();
    entity.setId(UUID.randomUUID());
    Item saved = new Item();
    saved.setId(entity.getId());
    ItemModel model = new ItemModel(saved.getId(), "item", "desc");

    when(itemMapper.toEntity(dto)).thenReturn(entity);
    when(itemRepository.save(entity)).thenReturn(saved);
    when(itemMapper.toModel(saved)).thenReturn(model);

    ItemModel result = service.create(dto, principal);

    assertNotNull(result);
    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(itemSearchService).indexItem(saved);
  }

  @Test
  void create_indexFails_stillReturnsModel() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    CreateItemDto dto = new CreateItemDto("item", "desc", orgId);
    Item entity = new Item();
    entity.setId(UUID.randomUUID());
    Item saved = new Item();
    saved.setId(entity.getId());
    ItemModel model = new ItemModel(saved.getId(), "item", "desc");

    when(itemMapper.toEntity(dto)).thenReturn(entity);
    when(itemRepository.save(entity)).thenReturn(saved);
    when(itemMapper.toModel(saved)).thenReturn(model);
    doThrow(new RuntimeException("vector store error")).when(itemSearchService).indexItem(saved);

    ItemModel result = service.create(dto, principal);

    assertNotNull(result);
    assertEquals(model.getId(), result.getId());
  }

  @Test
  void create_accessDenied_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID orgId = UUID.randomUUID();
    CreateItemDto dto = new CreateItemDto("item", "desc", orgId);
    doThrow(new NotFoundException()).when(accessService)
        .canManageWarehouseOrThrow(principal.getName(), orgId);

    assertThrows(NotFoundException.class, () -> service.create(dto, principal));
  }

  @Test
  void update_valid_updatesAndReindexes() {
    Principal principal = TestPrincipalFactory.create();
    UUID itemId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UpdateItemDto dto = new UpdateItemDto("new name", "new desc");
    Item item = new Item();
    item.setId(itemId);
    item.setOrganizationId(orgId);
    ItemModel model = new ItemModel(itemId, "new name", "new desc");

    when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
    when(itemRepository.save(item)).thenReturn(item);
    when(itemMapper.toModel(item)).thenReturn(model);

    ItemModel result = service.update(itemId, dto, principal);

    assertNotNull(result);
    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(itemMapper).updateEntity(item, dto);
    verify(itemSearchService).indexItem(item);
  }

  @Test
  void update_notFound_throws() {
    Principal principal = TestPrincipalFactory.create();
    UUID id = UUID.randomUUID();
    UpdateItemDto dto = new UpdateItemDto("name", null);
    when(itemRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.update(id, dto, principal));
  }

  @Test
  void delete_valid_removesFromVectorStoreAndDeletes() {
    Principal principal = TestPrincipalFactory.create();
    UUID itemId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    Item item = new Item();
    item.setId(itemId);
    item.setOrganizationId(orgId);

    when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

    service.delete(itemId, principal);

    verify(accessService).canManageWarehouseOrThrow(principal.getName(), orgId);
    verify(itemSearchService).removeItem(itemId);
    verify(itemRepository).delete(item);
  }

  @Test
  void delete_vectorStoreRemoveFails_stillDeletes() {
    Principal principal = TestPrincipalFactory.create();
    UUID itemId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    Item item = new Item();
    item.setId(itemId);
    item.setOrganizationId(orgId);

    when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
    doThrow(new RuntimeException("vector error")).when(itemSearchService).removeItem(itemId);

    assertDoesNotThrow(() -> service.delete(itemId, principal));
    verify(itemRepository).delete(item);
  }

  @Test
  void getOrThrow_found_returnsItem() {
    UUID id = UUID.randomUUID();
    Item item = new Item();
    item.setId(id);
    when(itemRepository.findById(id)).thenReturn(Optional.of(item));

    Item result = service.getOrThrow(id);

    assertEquals(id, result.getId());
  }

  @Test
  void getOrThrow_notFound_throws() {
    UUID id = UUID.randomUUID();
    when(itemRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getOrThrow(id));
  }
}
