package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonProductInfoResponse {

  private List<Item> items;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Item {

    private Long id;

    private String name;

    private List<String> barcodes;

    @JsonProperty("currency_code")
    private String currencyCode;

    private String price;

    @JsonProperty("model_info")
    private ModelInfo modelInfo;

    private Long sku;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ModelInfo {

      @JsonProperty("model_id")
      private Long modelId;
    }
  }
}
