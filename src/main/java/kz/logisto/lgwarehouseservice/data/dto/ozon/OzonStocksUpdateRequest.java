package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OzonStocksUpdateRequest {

  private List<StockItem> stocks;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StockItem {

    @JsonProperty("product_id")
    private Long productId;

    private int stock;

    @JsonProperty("warehouse_id")
    private Long warehouseId;
  }
}
