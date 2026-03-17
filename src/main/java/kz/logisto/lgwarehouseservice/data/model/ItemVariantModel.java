package kz.logisto.lgwarehouseservice.data.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemVariantModel {

  private UUID id;
  private String sku;
  private String barcode;
  private BigDecimal price;
  private Currency currency;
}
