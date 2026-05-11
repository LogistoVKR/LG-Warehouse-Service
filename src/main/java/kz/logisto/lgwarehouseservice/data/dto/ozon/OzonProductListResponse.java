package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonProductListResponse {

  private Result result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Result {

    private List<Item> items;

    @JsonProperty("last_id")
    private String lastId;

    private int total;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {

      @JsonProperty("product_id")
      private Long productId;
    }
  }
}
