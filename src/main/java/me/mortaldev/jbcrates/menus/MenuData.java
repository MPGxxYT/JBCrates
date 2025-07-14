package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.menus.factories.BasicMenu;

public class MenuData {
  private String search = "";
  private int page = 1;
  private Enum<? extends BasicMenu.IDisplayName> order;
  private Direction direction = Direction.ASCENDING;

  public String getSearch() {
    return search;
  }

  public void setSearch(String search) {
    this.search = search;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  // Add getters and setters for 'order'
  public Enum<? extends BasicMenu.IDisplayName> getOrder() {
    return order;
  }

  public void setOrder(Enum<? extends BasicMenu.IDisplayName> order) {
    this.order = order;
  }

  // Add a helper method to cycle to the next sorting option
  public void cycleOrder(Enum<? extends BasicMenu.IDisplayName>[] options) {
    if (options == null || options.length == 0) {
      this.order = null;
      return;
    }
    if (this.order == null) {
      this.order = options[0];
      return;
    }
    int currentIndex = -1;
    for (int i = 0; i < options.length; i++) {
      if (options[i] == this.order) {
        currentIndex = i;
        break;
      }
    }
    // Cycle to the next one, or wrap around to the start
    int nextIndex = (currentIndex + 1) % options.length;
    this.order = options[nextIndex];
  }

  public void toggleDirection() {
    this.direction =
        (this.direction == Direction.ASCENDING) ? Direction.DESCENDING : Direction.ASCENDING;
  }

  public enum Order implements BasicMenu.IDisplayName {
    ALPHABET("Alphabet"),
    DATE_CREATED("Date Created"),
    LAST_MODIFIED("Last Modified");

    private final String displayName;

    Order(String displayName) {
      this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }
  }

  public enum Direction {
    ASCENDING,
    DESCENDING
  }
}
