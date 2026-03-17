package kz.logisto.lgwarehouseservice.unit;

import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.mapper.ItemMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemService;
import kz.logisto.lgwarehouseservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

  @Mock
  private ItemMapper itemMapper;

  @Mock
  private AccessService accessService;

  @Mock
  private ItemRepository itemRepository;

  private ItemService service;

  @BeforeEach
  public void init() {
    this.service = new ItemServiceImpl(itemMapper, accessService, itemRepository);
  }
}
