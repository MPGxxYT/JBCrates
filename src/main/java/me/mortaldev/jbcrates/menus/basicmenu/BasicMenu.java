package me.mortaldev.jbcrates.menus.basicmenu;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * An abstract, generic base class for creating powerful, paginated, and interactive menus.
 *
 * <p>This class provides a robust framework for displaying collections of data with built-in
 * features for:
 *
 * <ul>
 *   <li>Pagination (up to 45 items per page).
 *   <li>Text-based searching.
 *   <li>Multi-criteria sorting with ascending/descending direction.
 *   <li>Filtering based on one or more enums.
 *   <li>A creation mechanism using an Anvil GUI for user input.
 * </ul>
 *
 * Implementations must provide the specific logic for displaying data, handling interactions, and
 * defining the available sorting and filtering options.
 *
 * @param <T> The type of identifiable data object this menu manages.
 * @version 1.0.5
 */
public abstract class BasicMenu<T extends CRUD.Identifiable> extends InventoryGUI {

  /**
   * The state object for the menu, storing the current page, search query, sorting, and filter
   * selections.
   */
  protected final MenuData menuData;

  /** The complete, unfiltered set of data to be displayed in the menu. */
  protected final Set<T> dataSet;

  /**
   * Constructs a new BasicMenu.
   *
   * @param menuData The data object that maintains the state of the menu (e.g., page, search).
   * @param dataSet The full set of data items to be displayed and managed by this menu.
   *     <p>example: super(menuData, LobbyManager.getInstance().getLobbies());
   */
  public BasicMenu(MenuData menuData, Set<T> dataSet) {
    this.menuData = menuData;
    this.dataSet = dataSet;
    if (menuData.getOrderId() == null
        && getOrderOptions() != null
        && !getOrderOptions().isEmpty()) {
      menuData.setOrderId(getOrderOptions().get(0).getId());
    }
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format(getInventoryName()));
  }

  /** Calculates the maximum number of pages based on the default search results. */
  private int getMaxPage() {
    return getMaxPage(applySearch(dataSet));
  }

  private int getMaxPage(Set<T> searchResults) {
    int searchResultSize = searchResults.size();
    if (searchResultSize == 0) {
      return 1;
    }
    return (int) Math.ceil(searchResultSize / 45.0);
  }

  /**
   * Determines the appropriate inventory size (2-6 rows) based on the number of items on the
   * current page.
   */
  private int getSize() {
    Set<T> searchResults = applySearch(dataSet);
    int maxPage = getMaxPage(searchResults);
    if (menuData.getPage() >= maxPage) {
      int totalItems = searchResults.size();
      if (totalItems == 0) return 2; // For control bar + empty row
      int itemsOnLastPage = totalItems % 45;
      if (itemsOnLastPage == 0) itemsOnLastPage = 45;
      return clamp((int) Math.ceil(itemsOnLastPage / 9.0) + 1);
    }
    return 6;
  }

  /** Clamps an integer value between a minimum of 2 and a maximum of 6. */
  private int clamp(int value) {
    return Math.max(2, Math.min(6, value));
  }

  /**
   * Filters the dataset based on the search query in {@link MenuData}.
   *
   * <p>This method requires that the generic type {@code T} implements the {@link Identifiable}
   * interface.
   *
   * @param dataSet The set of data to filter by search query.
   * @return A new set containing only the items that match the search query.
   */
  private Set<T> applySearch(Set<T> dataSet) {
    String search = menuData.getSearch();
    if (search == null || search.isEmpty()) {
      return dataSet;
    }
    return dataSet.stream()
        .filter(data -> data.getId().toLowerCase().contains(search.toLowerCase()))
        .collect(Collectors.toSet());
  }

  /**
   * Filters the dataset based on the selected enum filters in {@link MenuData}.
   *
   * @param dataSet The set of data to filter.
   * @return A new set containing only the items that match the active enum filters.
   */
  private Set<T> applyEnumFilters(Set<T> dataSet) {
    if (getEnumFilters() == null || getEnumFilters().isEmpty()) {
      return dataSet;
    }
    Set<T> filteredData = new HashSet<>(dataSet);
    for (EnumFilter<T, ?> filter :
        getEnumFilters()) { // The wildcard '?' is the source of the issue
      // We call a helper method to capture the wildcard type
      applySingleFilter(filteredData, filter);
    }
    return filteredData;
  }

  /**
   * Helper method to apply a single enum filter to a dataset. This method uses a generic capture to
   * resolve the wildcard type issue from `applyEnumFilters`.
   *
   * @param dataToFilter The set of data to be filtered (will be modified).
   * @param filter The specific {@link EnumFilter} to apply.
   * @param <E> The captured enum type that implements {@link MenuData.IDisplayName}.
   */
  private <E extends Enum<E> & MenuData.IDisplayName> void applySingleFilter(
      Set<T> dataToFilter, EnumFilter<T, E> filter) {
    menuData
        .getEnumFilter(filter.getFilterId())
        .ifPresent(
            filterValue -> {
              dataToFilter.removeIf(
                  data -> {
                    E dataValue = filter.getDataExtractor().apply(data);
                    return dataValue != null && !dataValue.equals(filterValue);
                  });
            });
  }

  /**
   * Reverses the order of the dataset if the direction in {@link MenuData} is DESCENDING.
   *
   * @param data The ordered set of data to potentially reverse.
   * @return A new {@link LinkedHashSet} with the final order.
   */
  private LinkedHashSet<T> applyDirection(LinkedHashSet<T> data) {
    if (menuData.getDirection() == MenuData.Direction.DESCENDING) {
      List<T> list = new ArrayList<>(data);
      Collections.reverse(list);
      return new LinkedHashSet<>(list);
    }
    return data;
  }

  /**
   * Paginates the dataset based on the current page number in {@link MenuData}.
   *
   * @param data The full, sorted, and filtered set of data to paginate.
   * @return A {@link LinkedHashSet} containing only the items for the current page.
   */
  private LinkedHashSet<T> applyPage(Set<T> data) {
    // No need to calculate maxPage here, as it's done in decorate()
    int maxPage = getMaxPage(data);
    int page = menuData.getPage();

    // If the current page is now out of bounds (e.g., after a search), reset it to the new max
    // page.
    if (page > maxPage) {
      page = maxPage;
      menuData.setPage(page);
    }

    return data.stream()
        .skip((long) (page - 1) * 45L) // Cast to long for safety with large numbers
        .limit(45)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Decorates the inventory with all buttons and items. This method orchestrates the filtering,
   * sorting, and pagination of data, then adds the data buttons and control buttons to the
   * inventory.
   *
   * @param player The player for whom the GUI is being decorated.
   */
  @Override
  public void decorate(Player player) {
    // Top control bar background
    ItemStack glass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int i = 0; i < 9; i++) {
      getInventory().setItem(i, glass);
    }

    // Process and display data
    Set<T> searchFiltered = applySearch(dataSet);
    Set<T> filtered = applyEnumFilters(searchFiltered);
    int maxPage = getMaxPage(filtered);
    LinkedHashSet<T> ordered = applyOrder(filtered, menuData);
    LinkedHashSet<T> directionApplied = applyDirection(ordered); // This reverses the ordered set
    LinkedHashSet<T> pageAdjusted = applyPage(directionApplied);

    int slot = 9;
    for (T data : pageAdjusted) {
      addButton(slot++, DataButton(data));
    }

    // Add control buttons
    // We can simplify the back button logic slightly
    if (backButton() != null || menuData.getPage() > 1) {
      addButton(0, BackButton());
    }

    // Add enum filter buttons
    if (hasCreateButton()) {
      addButton(6, CreateButton());
    }
    addButton(5, SearchButton());
    addButton(4, DirectionButton());
    int optionSlot = 3;
    if (getOrderOptions() != null && !getOrderOptions().isEmpty()) {
      addButton(optionSlot--, OrderButton());
    }
    List<EnumFilter<T, ?>> enumFilters = getEnumFilters();
    if (enumFilters != null && !enumFilters.isEmpty()) {
      addButton(optionSlot, FilterManagerButton());
    }
    // Check against the current page to show the next button
    if (menuData.getPage() < maxPage) {
      addButton(8, NextButton());
    }

    super.decorate(player);
  }

  /**
   * A factory method to create a new instance of the concrete menu implementation. This is
   * essential for refreshing the menu state correctly.
   *
   * @param menuData The current menu state.
   * @param dataSet The original, full dataset.
   * @return A new instance of this menu.
   */
  /**
   * A factory method to create a new instance of the concrete menu implementation. This is
   * essential for refreshing the menu state correctly.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public BasicMenu<Lobby> getNewInstance(MenuData menuData, Set<Lobby> dataSet) {
   *   return new LobbiesMenu(menuData); // dataSet is implicitly passed in the constructor
   * }
   * }</pre>
   */
  public abstract BasicMenu<T> getNewInstance(MenuData menuData, Set<T> dataSet);

  /**
   * Gets the name to be displayed at the top of the inventory.
   *
   * @return The inventory title.
   */
  /**
   * Gets the name to be displayed at the top of the inventory.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public String getInventoryName() { return "Available Lobbies"; }
   * }</pre>
   */
  public abstract String getInventoryName();

  /**
   * Defines the action to be performed when the back button is clicked on the first page. If this
   * returns null, the back button will not be displayed on the first page.
   *
   * @return A {@link Consumer} for the click event, or null to define no action.
   */
  /**
   * Defines the action to be performed when the back button is clicked on the first page. If this
   * returns null, the back button will not be displayed on the first page.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public Consumer<InventoryClickEvent> backButton() {
   *   return event -> GUIManager.getInstance().openGUI(new MainMenu(), (Player) event.getWhoClicked());
   * }
   * }</pre>
   */
  public abstract Consumer<InventoryClickEvent> backButton();

  /**
   * Determines whether the "Create" button should be displayed in the control bar.
   *
   * @return True to show the create button, false otherwise.
   */
  /**
   * Determines whether the "Create" button should be displayed in the control bar.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public boolean hasCreateButton() { return true; }
   * }</pre>
   */
  public abstract boolean hasCreateButton();

  /**
   * Defines the action to be performed when a new data item is created via the Anvil GUI.
   *
   * @param textEntry The sanitized ID entered by the user.
   * @param event The original click event that initiated the creation.
   * @param menuData The current menu state.
   * @return A {@link Runnable} containing the logic to create and persist the new data item.
   */
  /**
   * Defines the action to be performed when a new data item is created via the Anvil GUI.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public Runnable createNewData(String textEntry, InventoryClickEvent event, MenuData menuData) {
   *   return () -> {
   *     Player player = (Player) event.getWhoClicked();
   *     Lobby newLobby = new Lobby(textEntry, ...);
   *     LobbyManager.getInstance().add(newLobby);
   *     player.sendMessage("Lobby " + textEntry + " created!");
   *   };
   * }
   * }</pre>
   */
  public abstract Runnable createNewData(
      String textEntry, InventoryClickEvent event, MenuData menuData);

  /**
   * Creates the {@link ItemStack} used to represent a single data item in the menu.
   *
   * @param data The data item to display.
   * @param player The player viewing the menu.
   * @return The visual representation of the data item.
   */
  /**
   * Creates the {@link ItemStack} used to represent a single data item in the menu.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public ItemStack getDataButtonDisplayStack(Lobby data, Player player) {
   *   return ItemStackHelper.builder(Material.BEACON)
   *       .name("&a" + data.getId())
   *       .addLore("&7Status: " + data.getStatus().getDisplayName())
   *       .addLore("&7Players: " + data.getProfiles().size())
   *       .build();
   * }
   * }</pre>
   */
  public abstract ItemStack getDataButtonDisplayStack(T data, Player player);

  /**
   * Defines the action to be performed when a data item's button is clicked.
   *
   * @param data The data item associated with the clicked button.
   * @param menuData The current menu state.
   * @return A {@link Consumer} for the click event.
   */
  /**
   * Defines the action to be performed when a data item's button is clicked.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public Consumer<InventoryClickEvent> dataButtonClickConsumer(Lobby data, MenuData menuData) {
   *   return event -> {
   *     data.join((Player) event.getWhoClicked());
   *   };
   * }
   * }</pre>
   */
  public abstract Consumer<InventoryClickEvent> dataButtonClickConsumer(T data, MenuData menuData);

  /**
   * Gets the sound to be played when an action is denied (e.g., invalid input).
   *
   * @return The deny {@link Sound}.
   */
  /**
   * Gets the sound to be played when an action is denied (e.g., invalid input).
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public Sound getDenySound() { return Sound.BLOCK_NOTE_BLOCK_BASS; }
   * }</pre>
   */
  public abstract Sound getDenySound();

  /**
   * Provides the list of available sorting options for this menu.
   *
   * @return A list of {@link OrderOption} objects.
   */
  /**
   * Provides the list of available sorting options for this menu.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public List<OrderOption<Lobby>> getOrderOptions() {
   *   return List.of(
   *       new OrderOption<>("id", "Alphabetical", Comparator.comparing(Lobby::getId)),
   *       new OrderOption<>("players", "Player Count", Comparator.comparingInt(l -> l.getProfiles().size()).reversed())
   *   );
   * }
   * }</pre>
   */
  public abstract List<OrderOption<T>> getOrderOptions();

  /**
   * Provides the list of available enum-based filters for this menu.
   *
   * @return A list of {@link EnumFilter} objects.
   */
  /**
   * Provides the list of available enum-based filters for this menu.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * @Override
   * public List<EnumFilter<Lobby, ?>> getEnumFilters() {
   *   return List.of(
   *       new EnumFilter<>("gamemode", "Gamemode", Gamemode.class, Lobby::getGamemode),
   *       new EnumFilter<>("status", "Status", LobbyStatus.class, Lobby::getStatus)
   *   );
   * }
   * }</pre>
   */
  public abstract List<EnumFilter<T, ?>> getEnumFilters();

  /**
   * Applies the currently selected sorting option to the dataset.
   *
   * @param data The set of data to be sorted.
   * @param menuData The current menu state, containing the selected order ID.
   * @return A {@link LinkedHashSet} with the data sorted according to the selected comparator.
   */
  public LinkedHashSet<T> applyOrder(Set<T> data, MenuData menuData) {
    Optional<OrderOption<T>> selectedOption =
        getOrderOptions().stream()
            .filter(option -> option.getId().equals(menuData.getOrderId()))
            .findFirst();

    // Return unsorted if no option is found
    return selectedOption
        .map(
            tOrderOption ->
                data.stream()
                    .sorted(tOrderOption.getComparator())
                    .collect(Collectors.toCollection(LinkedHashSet::new)))
        .orElseGet(() -> new LinkedHashSet<>(data));
  }

  /** Creates the inventory button for a single data item. */
  private InventoryButton DataButton(T data) {
    return new InventoryButton()
        .creator(player -> getDataButtonDisplayStack(data, player))
        .consumer(dataButtonClickConsumer(data, menuData));
  }

  /** Creates the "Order By" button, which cycles through the available {@link OrderOption}s. */
  private InventoryButton OrderButton() {
    String currentOrderId = menuData.getOrderId();
    List<String> middleLore = new ArrayList<>();
    for (OrderOption<T> orderOption : getOrderOptions()) {
      String displayName = orderOption.getDisplayName();
      if (orderOption.getId().equals(currentOrderId)) {
        middleLore.add("&f> &l" + displayName);
      } else {
        middleLore.add("&7  " + displayName);
      }
    }

    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.COMPARATOR)
                    .name("&3&lOrder By")
                    .addLore("&7Change the sorting order of the items.")
                    .addLore()
                    .addLore(middleLore)
                    .addLore()
                    .addLore("&7( click to cycle )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              updateMenu(player, () -> menuData.cycleOrder(getOrderOptions()));
            });
  }

  /** Creates the "Direction" button, which toggles between ASCENDING and DESCENDING order. */
  private InventoryButton DirectionButton() {
    boolean isAscending = menuData.getDirection() == MenuData.Direction.ASCENDING;
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(isAscending ? Material.ENDER_PEARL : Material.ENDER_EYE)
                    .name("&3&lDirection")
                    .addLore("&7Change the sorting direction.")
                    .addLore()
                    .addLore(
                        isAscending
                            ? "&f&lASCENDING &8/ &7DESCENDING"
                            : "&7ASCENDING &8/ &f&lDESCENDING")
                    .addLore()
                    .addLore("&7( click to toggle )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              updateMenu(player, menuData::toggleDirection);
            });
  }

  /** Creates a single, powerful button to manage all available filters. */
  private InventoryButton FilterManagerButton() {
    return new InventoryButton()
        .creator(
            player -> {
              List<EnumFilter<T, ?>> filters = getEnumFilters();
              int activeIndex = menuData.getActiveFilterIndex();

              ItemStackHelper.Builder builder =
                  ItemStackHelper.builder(Material.HOPPER)
                      .name("&3&lFilters")
                      .addLore("&7Manage and apply filters to the list.")
                      .addLore("");
              for (int i = 0; i < filters.size(); i++) {
                if (i == activeIndex) {
                  // Add the expanded view for the active filter
                  addExpandedFilterLore(builder, filters.get(i));
                } else {
                  // Add the collapsed view for inactive filters
                  addCollapsedFilterLore(builder, filters.get(i));
                }
              }

              builder
                  .addLore("")
                  .addLore("&bRight-Click: &7Next filter.")
                  .addLore("&bLeft-Click: &7Change filter option.");

              return builder.build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              List<EnumFilter<T, ?>> filters = getEnumFilters();
              if (filters.isEmpty()) return;

              if (event.isRightClick()) {
                menuData.cycleActiveFilter(filters.size());
              } else {
                // Left-Click logic to cycle the active filter's value
                cycleActiveFilterValue(filters.get(menuData.getActiveFilterIndex()));
              }
              // Refresh the menu without resetting the page
              GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
              player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
            });
  }

  /** Adds the lore for a collapsed (inactive) filter to the builder. */
  private void addCollapsedFilterLore(ItemStackHelper.Builder builder, EnumFilter<T, ?> filter) {
    String currentSetting =
        menuData
            .getEnumFilter(filter.getFilterId())
            .map(val -> ((MenuData.IDisplayName) val).getDisplayName())
            .orElse("All");
    builder.addLore("&7  " + filter.getFilterName() + ": &f" + currentSetting);
  }

  /** Adds the lore for an expanded (active) filter, showing all its options. */
  private <E extends Enum<E> & MenuData.IDisplayName> void addExpandedFilterLore(
      ItemStackHelper.Builder builder, EnumFilter<T, E> filter) {

    Optional<Enum<?>> currentFilterValue = menuData.getEnumFilter(filter.getFilterId());
    builder.addLore(
        "&f > "
            + filter.getFilterName()
            + ": "
            + (currentFilterValue.isEmpty()
                ? "All"
                : ((MenuData.IDisplayName) currentFilterValue.get()).getDisplayName()));

    // "All" option
    if (currentFilterValue.isEmpty()) {
      builder.addLore("  &f > &lAll");
    } else {
      builder.addLore("  &7   All");
    }

    // Enum options
    for (E enumValue : filter.getEnumClass().getEnumConstants()) {
      if (currentFilterValue.isPresent() && currentFilterValue.get().equals(enumValue)) {
        builder.addLore("  &f > &l" + enumValue.getDisplayName());
      } else {
        builder.addLore("  &7   " + enumValue.getDisplayName());
      }
    }
  }

  /** Helper method to cycle through the values of the currently active filter. */
  private <E extends Enum<E> & MenuData.IDisplayName> void cycleActiveFilterValue(
      EnumFilter<T, E> filter) {
    E[] enumValues = filter.getEnumClass().getEnumConstants();
    if (enumValues.length == 0) return;

    Optional<Enum<?>> currentFilterValue = menuData.getEnumFilter(filter.getFilterId());

    if (currentFilterValue.isEmpty()) {
      // If current is "All", cycle to the first enum value
      menuData.setEnumFilter(filter.getFilterId(), enumValues[0]);
    } else {
      // Find the index of the current value in the array of all possible values.
      int currentIndex = -1;
      for (int i = 0; i < enumValues.length; i++) {
        if (enumValues[i].equals(currentFilterValue.get())) {
          currentIndex = i;
          break;
        }
      }
      if (currentIndex == -1 || currentIndex == enumValues.length - 1) {
        // If it's the last enum value, cycle back to "All"
        menuData.getEnumFilters().remove(filter.getFilterId());
      } else {
        // Cycle to the next enum value
        menuData.setEnumFilter(filter.getFilterId(), enumValues[currentIndex + 1]);
      }
    }
  }

  /** Creates the "Create" button, which opens an Anvil GUI for user input. */
  private InventoryButton CreateButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.LIME_DYE)
                    .name("&2&lCreate")
                    .addLore("&7Creates a new instance to manage.")
                    .addLore()
                    .addLore("&7( click to create )")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Create")
                  .itemLeft(ItemStackHelper.builder(Material.PAPER).name("id").build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText().trim();
                          if (textEntry.isBlank()) {
                            player.sendMessage("&cMust enter an id!");
                            player.playSound(player.getLocation(), getDenySound(), 0.5f, 0.75f);
                            GUIManager.getInstance()
                                .openGUI(getNewInstance(menuData, dataSet), player);
                            return Collections.emptyList();
                          }
                          String finalTextEntry = textEntry;
                          // Assumes T implements Identifiable
                          if (dataSet.stream()
                              .anyMatch(d -> d.getId().equalsIgnoreCase(finalTextEntry))) {
                            player.sendMessage("&cAn entry with that ID already exists!");
                            player.playSound(player.getLocation(), getDenySound(), 0.5f, 0.75f);
                            GUIManager.getInstance()
                                .openGUI(getNewInstance(menuData, dataSet), player);
                            return Collections.emptyList();
                          }
                          textEntry = textEntry.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
                          createNewData(textEntry, event, menuData).run();
                          return Collections.emptyList();
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  /**
   * Creates the "Search" button, which opens an Anvil GUI for query input or clears the current
   * search.
   */
  private InventoryButton SearchButton() {
    return new InventoryButton()
        .creator(
            player -> {
              ItemStackHelper.Builder builder =
                  ItemStackHelper.builder(Material.ANVIL)
                      .name("&3&lSearch")
                      .addLore("&7Enter a search query to find something specific.")
                      .addLore("");
              if (menuData.getSearch() != null && !menuData.getSearch().isBlank()) {
                builder
                    .addLore("&7Query: &f" + menuData.getSearch())
                    .addLore("")
                    .addLore("&7( click to clear )");
              } else {
                builder.addLore("&7Query: &fNone").addLore("").addLore("&7( click to search )");
              }
              return builder.build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 0.75f);
              if (menuData.getSearch() != null && !menuData.getSearch().isBlank()) {
                updateMenu(player, () -> menuData.setSearch(""));
                return;
              }
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Search")
                  .itemLeft(ItemStackHelper.builder(Material.PAPER).name(" ").build())
                  .onClick((slot, stateSnapshot) -> handleSearchInput(slot, stateSnapshot, player))
                  .open(player);
            });
  }

  /**
   * Creates the "Back" button, which navigates to the previous page or triggers the custom back
   * action.
   */
  private InventoryButton BackButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to the previous page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              int page = menuData.getPage();
              if (page > 1) {
                menuData.setPage(page - 1);
                GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
              } else {
                // This handles the case where page is 1 but a back action is defined
                if (backButton() != null) {
                  backButton().accept(event);
                }
              }
            });
  }

  /** Creates the "Next" button, which navigates to the next page. */
  private InventoryButton NextButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&a&lNext")
                    .addLore("&7Click to go to the next page.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (menuData.getPage() < getMaxPage()) {
                menuData.setPage(menuData.getPage() + 1);
              }
              GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
            });
  }

  /**
   * A helper method to perform an action, reset the page to 1, and refresh the menu.
   *
   * @param player The player viewing the menu.
   * @param action The {@link Runnable} action to perform before refreshing (e.g., changing a
   *     filter).
   */
  private void updateMenu(Player player, Runnable action) {
    action.run();
    menuData.setPage(1);
    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
    GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
  }

  /** Handles the input from the search Anvil GUI. */
  private List<AnvilGUI.ResponseAction> handleSearchInput(
      int slot, AnvilGUI.StateSnapshot stateSnapshot, Player player) {
    if (slot == AnvilGUI.Slot.OUTPUT) {
      String textEntry = stateSnapshot.getText().trim();
      menuData.setSearch(textEntry);
      menuData.setPage(1);
      // We need to schedule the GUI opening for the next tick to avoid conflicts with AnvilGUI
      // closing.
      Bukkit.getScheduler()
          .runTask(
              Main.getInstance(),
              () -> {
                GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
                player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 1f);
              });
    }
    return Collections.emptyList();
  }

  /**
   * A contract for data objects that can be uniquely identified by a string ID. This is required
   * for features like searching and duplicate-checking on creation.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * public class Lobby implements BasicMenu.Identifiable {
   *   private final String id;
   *   // ...
   *   @Override
   *   public String getId() { return this.id; }
   * }
   * }</pre>
   */
  public interface Identifiable {
    /**
     * @return The unique string ID of the object.
     */
    String getId();
  }
}
