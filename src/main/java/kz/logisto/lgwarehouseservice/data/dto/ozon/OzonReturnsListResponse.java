package kz.logisto.lgwarehouseservice.data.dto.ozon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonReturnsListResponse {

  private List<Return> returns;

  @JsonProperty("has_next")
  private boolean hasNext;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Return {

    private long id;

    @JsonProperty("posting_number")
    private String postingNumber;

    private String schema;

    private Product product;

    private Place place;

    private Visual visual;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Product {

    private Long sku;

    private int quantity;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Place {

    private Long id;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Visual {

    private Status status;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Status {

      @JsonProperty("sys_name")
      private String sysName;

      @JsonProperty("display_name")
      private String displayName;
    }
  }
}
