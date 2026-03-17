package kz.logisto.lgwarehouseservice.data.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemModel {

  private UUID id;
  private String name;
  private String description;
}
