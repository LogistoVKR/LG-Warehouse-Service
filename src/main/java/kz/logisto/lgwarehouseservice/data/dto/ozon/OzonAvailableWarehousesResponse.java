package kz.logisto.lgwarehouseservice.data.dto.ozon;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OzonAvailableWarehousesResponse {

  private List<WarehouseAvailabilityItem> result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class WarehouseAvailabilityItem {

    private WarehouseInfo warehouse;
    private Schedule schedule;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class WarehouseInfo {

    private String id;
    private String name;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Schedule {

    private List<CapacityEntry> capacity;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class CapacityEntry {

    private long value;
  }
}
