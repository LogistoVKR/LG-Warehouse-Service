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
public class MovementAnalyticsModel {

  private String summary;
  private List<DeficitPrediction> deficitPredictions;
  private List<SurplusPrediction> surplusPredictions;
  private List<MovementRecommendation> recommendations;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeficitPrediction {

    private String itemVariantSku;
    private String pointOfStorageName;
    private Integer currentQuantity;
    private Integer predictedDeficit;
    private String reasoning;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SurplusPrediction {

    private String itemVariantSku;
    private String pointOfStorageName;
    private Integer currentQuantity;
    private Integer estimatedSurplus;
    private String reasoning;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MovementRecommendation {

    private String action;
    private String itemVariantSku;
    private String fromLocation;
    private String toLocation;
    private Integer suggestedQuantity;
    private String reasoning;
  }
}
