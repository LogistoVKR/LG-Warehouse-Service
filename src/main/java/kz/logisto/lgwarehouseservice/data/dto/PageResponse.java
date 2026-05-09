package kz.logisto.lgwarehouseservice.data.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PageResponse<T> {

  private List<T> content;
  private boolean last;
}
