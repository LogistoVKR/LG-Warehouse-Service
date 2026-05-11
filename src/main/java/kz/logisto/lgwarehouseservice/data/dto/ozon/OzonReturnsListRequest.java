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
public class OzonReturnsListRequest {

  private Filter filter;
  private int limit;

  @JsonProperty("last_id")
  private long lastId;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Filter {

    @JsonProperty("logistic_return_date")
    private TimeRange logisticReturnDate;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TimeRange {

    @JsonProperty("time_from")
    private String timeFrom;

    @JsonProperty("time_to")
    private String timeTo;
  }
}
