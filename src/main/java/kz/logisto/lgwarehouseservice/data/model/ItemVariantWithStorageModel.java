package kz.logisto.lgwarehouseservice.data.model;

import java.math.BigDecimal;
import java.util.Currency;
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
public class ItemVariantWithStorageModel {

  private UUID id;
  private String sku;
  private String barcode;
  private BigDecimal price;
  private Currency currency;
  private List<ItemVariantPointOfStorageModel> storageLocations;
}
