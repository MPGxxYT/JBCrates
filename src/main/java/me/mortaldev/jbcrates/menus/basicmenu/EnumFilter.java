package me.mortaldev.jbcrates.menus.basicmenu;

import java.util.function.Function;

/**
 * Represents a filter based on an Enum. This class encapsulates the logic and properties required
 * to filter a dataset of type T by an enum property.
 *
 * @param <T> The type of the data object being filtered.
 * @param <E> The enum type used for filtering.
 */
public class EnumFilter<T, E extends Enum<E> & MenuData.IDisplayName> {

  private final String filterId;
  private final String filterName;
  private final Class<E> enumClass;
  private final Function<T, E> dataExtractor;

  /**
   * @param filterId A unique identifier for the filter (e.g., "gamemode").
   * @param filterName The display name for the filter button (e.g., "Gamemode").
   * @param enumClass The class of the enum being used for filtering.
   * @param dataExtractor A function to extract the enum value from a data object.
   */
  public EnumFilter(
      String filterId, String filterName, Class<E> enumClass, Function<T, E> dataExtractor) {
    this.filterId = filterId;
    this.filterName = filterName;
    this.enumClass = enumClass;
    this.dataExtractor = dataExtractor;
  }

  public String getFilterId() {
    return filterId;
  }

  public String getFilterName() {
    return filterName;
  }

  public Class<E> getEnumClass() {
    return enumClass;
  }

  public Function<T, E> getDataExtractor() {
    return dataExtractor;
  }
}
