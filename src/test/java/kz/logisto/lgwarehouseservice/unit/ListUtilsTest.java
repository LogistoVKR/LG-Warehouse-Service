package kz.logisto.lgwarehouseservice.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kz.logisto.lgwarehouseservice.exception.NotEqualValuesException;
import kz.logisto.lgwarehouseservice.util.ListUtils;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListUtilsTest {

  @Test
  void getLastInEquals_allEqual_returnsLast() {
    UUID id = UUID.randomUUID();
    UUID result = ListUtils.getLastInEquals(List.of(id, id, id));
    assertEquals(id, result);
  }

  @Test
  void getLastInEquals_singleElement_returnsThat() {
    UUID id = UUID.randomUUID();
    UUID result = ListUtils.getLastInEquals(List.of(id));
    assertEquals(id, result);
  }

  @Test
  void getLastInEquals_twoEqual_returnsLast() {
    UUID id = UUID.randomUUID();
    UUID result = ListUtils.getLastInEquals(List.of(id, id));
    assertEquals(id, result);
  }

  @Test
  void getLastInEquals_notEqual_throwsNotEqualValuesException() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    assertThrows(NotEqualValuesException.class,
        () -> ListUtils.getLastInEquals(List.of(id1, id2)));
  }

  @Test
  void getLastInEquals_threeElementsLastDiffers_throws() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    assertThrows(NotEqualValuesException.class,
        () -> ListUtils.getLastInEquals(List.of(id1, id1, id2)));
  }
}
