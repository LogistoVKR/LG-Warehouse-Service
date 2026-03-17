package kz.logisto.lgwarehouseservice.util;

import kz.logisto.lgwarehouseservice.exception.NotEqualValuesException;
import java.util.List;

public final class ListUtils {

  private ListUtils() { }

  public static <T> T getLastInEquals(List<T> values) throws NotEqualValuesException {
    T previous = values.getFirst();
    for (int i = 1; i < values.size(); ++i) {
      if (!previous.equals(values.get(i))) {
        throw new NotEqualValuesException();
      }
      previous = values.get(i);
    }
    return previous;
  }
}
