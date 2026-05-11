package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonStocksUpdateResponse {

  private List<ResultItem> result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class ResultItem {

    @JsonProperty("product_id")
    private Long productId;

    private boolean updated;

    private List<StockError> errors;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class StockError {

    private String code;

    private String message;
  }
}
