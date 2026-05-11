package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonStockOnWarehousesResponse {

  private Result result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Result {

    private List<Row> rows;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Row {

    private Long sku;

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("free_to_sell_amount")
    private int freeToSellAmount;

    @JsonProperty("warehouse_id")
    private Long warehouseId;

    @JsonProperty("warehouse_name")
    private String warehouseName;
  }
}
