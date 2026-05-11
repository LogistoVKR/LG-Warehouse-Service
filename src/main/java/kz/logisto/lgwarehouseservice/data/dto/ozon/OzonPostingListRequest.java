package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OzonPostingListRequest {

  private Filter filter;
  private int limit;
  private int offset;
  private With with;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Filter {

    private String since;

    private String to;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class With {

    @JsonProperty("financial_data")
    private boolean financialData;
  }
}
