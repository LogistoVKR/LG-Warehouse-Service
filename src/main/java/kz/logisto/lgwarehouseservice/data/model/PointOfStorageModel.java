package kz.logisto.lgwarehouseservice.data.model;

import kz.logisto.lgwarehouseservice.data.enums.PointOfStorageType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointOfStorageModel {

  private UUID id;
  private String name;
  private String description;
  private String location;
  private PointOfStorageType type;
}
