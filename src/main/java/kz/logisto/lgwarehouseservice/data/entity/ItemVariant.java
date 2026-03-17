package kz.logisto.lgwarehouseservice.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_variant")
public class ItemVariant {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String sku;

  private String barcode;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.MERGE, CascadeType.DETACH})
  @JoinColumn(name = "item_id")
  private Item item;

  private BigDecimal price;

  private Currency currency;

  @Transient
  @JsonIgnore
  public UUID getOrganizationId() {
    return item.getOrganizationId();
  }
}
