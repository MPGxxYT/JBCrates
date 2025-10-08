package me.mortaldev.jbcrates.menus.basicmenu;

import java.util.Comparator;

/**
 * Represents a sorting option for a BasicMenu. This class encapsulates the logic and properties
 * required to sort a dataset of type T.
 *
 * @param <T> The type of the data object being sorted.
 */
public class OrderOption<T> {

  private final String id;
  private final String displayName;
  private final Comparator<T> comparator;

  /**
   * @param id A unique identifier for the sorting option (e.g., "alphabetical").
   * @param displayName The display name for the order button (e.g., "Alphabetical").
   * @param comparator The comparator that defines the sorting logic.
   */
  public OrderOption(String id, String displayName, Comparator<T> comparator) {
    this.id = id;
    this.displayName = displayName;
    this.comparator = comparator;
  }

  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Comparator<T> getComparator() {
    return comparator;
  }
}
