package kz.logisto.lgwarehouseservice.data.dto.ozon;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OzonSubscriptionRequest {

  private List<String> skus;
}
