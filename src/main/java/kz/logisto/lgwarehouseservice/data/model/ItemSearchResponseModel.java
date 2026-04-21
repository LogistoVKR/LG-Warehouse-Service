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
public class ItemSearchResponseModel {

  private String query;
  private int totalResults;
  private List<ItemSearchResultModel> results;
}
