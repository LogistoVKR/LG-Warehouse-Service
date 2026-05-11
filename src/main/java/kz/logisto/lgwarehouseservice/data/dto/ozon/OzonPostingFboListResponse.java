package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonPostingFboListResponse {

  private List<Posting> result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Posting {

    @JsonProperty("posting_number")
    private String postingNumber;

    private String status;

    @JsonProperty("analytics_data")
    private AnalyticsData analyticsData;

    private List<Product> products;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class AnalyticsData {

    @JsonProperty("warehouse_id")
    private Long warehouseId;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Product {

    private Long sku;

    private int quantity;
  }
}
