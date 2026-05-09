package kz.logisto.lgwarehouseservice.data.dto.ozon;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonProductDescriptionResponse {

  private Result result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Result {

    private Long id;

    private String name;

    private String description;
  }
}
