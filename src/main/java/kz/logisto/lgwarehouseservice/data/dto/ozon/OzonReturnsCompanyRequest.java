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
public class OzonReturnsCompanyRequest {

  private Filter filter;
  private int limit;
  private int offset;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Filter {

    @JsonProperty("updated_at_from")
    private String updatedAtFrom;
  }
}
