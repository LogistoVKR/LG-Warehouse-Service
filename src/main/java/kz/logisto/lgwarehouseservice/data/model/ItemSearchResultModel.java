package kz.logisto.lgwarehouseservice.data.model;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchResultModel {

  private UUID itemId;
  private String name;
  private String description;
  private UUID organizationId;
  private double score;
  private List<ItemVariantWithStorageModel> variants;
}
