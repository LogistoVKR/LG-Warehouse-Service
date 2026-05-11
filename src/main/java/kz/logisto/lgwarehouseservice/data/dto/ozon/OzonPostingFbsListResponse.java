package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonPostingFbsListResponse {

  private Result result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Result {

    private List<Posting> postings;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Posting {

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

    private BigDecimal price;

    @JsonProperty("currency_code")
    private String currencyCode;
  }
}
