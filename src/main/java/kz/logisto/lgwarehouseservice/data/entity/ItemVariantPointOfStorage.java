package kz.logisto.lgwarehouseservice.data.entity;

import kz.logisto.lgwarehouseservice.data.entity.key.ItemVariantPointOfStorageId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
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
@Table(name = "item_variant_point_of_storage")
public class ItemVariantPointOfStorage {

  @EmbeddedId
  private ItemVariantPointOfStorageId id;

  private int quantity;

  private int reserved;

  @Fetch(FetchMode.JOIN)
  @MapsId("pointOfStorageId")
  @JoinColumn(name = "point_of_storage_id")
  @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
  private PointOfStorage pointOfStorage;
}
