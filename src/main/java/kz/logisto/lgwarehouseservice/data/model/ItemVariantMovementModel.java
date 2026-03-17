package kz.logisto.lgwarehouseservice.data.model;

import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class ItemVariantMovementModel {

  private UUID id;
  private PointOfStorageModel fromPointOfStorage;
  private PointOfStorageModel toPointOfStorage;
  private ItemVariantModel itemVariant;
  private BigDecimal pricePerItem;
  private Currency currency;
  private Integer quantity;
  private String reason;
  private MovementType type;
  private LocalDateTime created;
}
