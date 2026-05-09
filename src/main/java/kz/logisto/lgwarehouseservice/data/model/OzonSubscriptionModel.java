package kz.logisto.lgwarehouseservice.data.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OzonSubscriptionModel {

  private ItemModel item;
  private List<OzonSubscriptionVariantModel> variants;
}
