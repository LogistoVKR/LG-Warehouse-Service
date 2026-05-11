package kz.logisto.lgwarehouseservice.data.dto.ozon;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonSubscriptionResponse {

  private List<SubscriptionEntry> result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class SubscriptionEntry {

    private Long sku;
    private Long count;
  }
}
