package kz.logisto.lgwarehouseservice.service;

import java.security.Principal;
import java.util.UUID;
import kz.logisto.lgwarehouseservice.data.dto.search.ItemSearchDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.model.ItemSearchResponseModel;

public interface ItemSearchService {

  ItemSearchResponseModel search(ItemSearchDto dto);

  void indexItem(Item item);

  void indexAllItems(UUID organizationId, Principal principal);

  void removeItem(UUID itemId);
}
