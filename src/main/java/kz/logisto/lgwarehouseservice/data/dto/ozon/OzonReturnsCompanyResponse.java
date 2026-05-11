package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonReturnsCompanyResponse {

  private List<ReturnItem> returns;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class ReturnItem {

    private Long id;

    @JsonProperty("posting_number")
    private String postingNumber;

    private String status;

    @JsonProperty("warehouse_id")
    private Long warehouseId;

    private List<Product> products;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Product {

    private Long sku;

    private int quantity;
  }
}
