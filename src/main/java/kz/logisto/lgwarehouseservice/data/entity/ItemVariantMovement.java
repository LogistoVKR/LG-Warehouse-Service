package kz.logisto.lgwarehouseservice.data.entity;

import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_variant_movement")
public class ItemVariantMovement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @JoinColumn(name = "from_point_of_storage_id")
  @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
  private PointOfStorage fromPointOfStorage;

  @JoinColumn(name = "to_point_of_storage_id")
  @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
  private PointOfStorage toPointOfStorage;

  @JoinColumn(name = "item_variant_id")
  @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
  private ItemVariant itemVariant;

  private BigDecimal pricePerItem;

  private Currency currency;

  private Integer quantity;

  private String reason;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private MovementType type;

  private LocalDateTime created;

  private UUID organizationId;

  public UUID getFromPointOfStorageId() {
    return fromPointOfStorage != null ? fromPointOfStorage.getId() : null;
  }

  public  UUID getToPointOfStorageId() {
    return toPointOfStorage != null ? toPointOfStorage.getId() : null;
  }

  public UUID getItemVariantId() {
    return itemVariant != null ? itemVariant.getId() : null;
  }
}
