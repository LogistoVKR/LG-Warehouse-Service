package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OzonProductListRequest {

  private Filter filter;

  @JsonProperty("last_id")
  private String lastId;

  private int limit;

  @Getter
  @AllArgsConstructor
  public static class Filter {

    private String visibility;
  }
}
