package kz.logisto.lgwarehouseservice.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.repository.ItemRepository;
import kz.logisto.lgwarehouseservice.integration.BaseIntegrationTest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ItemRepositoryTest extends BaseIntegrationTest {

  @Autowired
  private ItemRepository itemRepository;

  @Test
  void save_andFindById() {
    Item item = new Item();
    item.setName("Laptop");
    item.setDescription("High-end laptop");
    item.setOrganizationId(UUID.randomUUID());

    Item saved = itemRepository.save(item);
    assertNotNull(saved.getId());

    Optional<Item> found = itemRepository.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("Laptop", found.get().getName());
    assertEquals("High-end laptop", found.get().getDescription());
  }

  @Test
  void findAllByOrganizationId_returnsFiltered() {
    UUID org1 = UUID.randomUUID();
    UUID org2 = UUID.randomUUID();

    Item item1 = new Item();
    item1.setName("Item 1");
    item1.setOrganizationId(org1);
    itemRepository.save(item1);

    Item item2 = new Item();
    item2.setName("Item 2");
    item2.setOrganizationId(org1);
    itemRepository.save(item2);

    Item item3 = new Item();
    item3.setName("Item 3");
    item3.setOrganizationId(org2);
    itemRepository.save(item3);

    List<Item> org1Items = itemRepository.findAllByOrganizationId(org1);

    assertEquals(2, org1Items.size());
    assertTrue(org1Items.stream().allMatch(i -> i.getOrganizationId().equals(org1)));
  }

  @Test
  void findAllByOrganizationId_emptyForUnknownOrg() {
    List<Item> result = itemRepository.findAllByOrganizationId(UUID.randomUUID());
    assertTrue(result.isEmpty());
  }
}
