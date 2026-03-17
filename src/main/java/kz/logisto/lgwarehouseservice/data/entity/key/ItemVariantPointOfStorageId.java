package kz.logisto.lgwarehouseservice.data.entity.key;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ItemVariantPointOfStorageId implements Serializable {

  private UUID itemVariantId;
  private UUID pointOfStorageId;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ItemVariantPointOfStorageId that = (ItemVariantPointOfStorageId) o;
    return Objects.equals(itemVariantId, that.itemVariantId) && Objects.equals(
        pointOfStorageId, that.pointOfStorageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemVariantId, pointOfStorageId);
  }
}
