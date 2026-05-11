package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonWarehouseListResponse {

  private List<WarehouseItem> warehouses;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class WarehouseItem {

    @JsonProperty("warehouse_id")
    private Long warehouseId;

    private String name;
  }
}
