package me.mortaldev.jbcrates.menus.factories;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.menus.MenuData;
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
 * v1.0.3
 *
 * <p>An abstract class representing a basic paginated menu for displaying and managing data. This
 * menu provides functionality for searching, creating, sorting, and navigating through a set of
 * identifiable data objects.
 *
 * <p>
 *
 * @param <T> The type of identifiable data object this menu manages.
 */
public abstract class BasicMenu<T extends CRUD.Identifiable> extends InventoryGUI {

  protected final MenuData menuData;
  protected final Set<T> dataSet;

  public BasicMenu(MenuData menuData, Set<T> dataSet) {
    this.menuData = menuData;
    this.dataSet = dataSet;
    if (menuData.getOrder() == null && getOrderOptions() != null && getOrderOptions().length > 0) {
      menuData.setOrder(getOrderOptions()[0]);
    }
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, getSize() * 9, TextUtil.format(getInventoryName()));
  }

  private int getMaxPage() {
    int searchResultSize = applySearch(dataSet).size();
    if (searchResultSize == 0) {
      return 1;
    }
    return (int) Math.ceil(searchResultSize / 45.0);
  }

  private int getSize() { // Paginated
    int maxPage = getMaxPage();
    if (menuData.getPage() >= maxPage) {
      int mod = (maxPage - 1) * 45;
      int size = applySearch(dataSet).size();
      if (mod > 0) {
        size = size % mod;
        if (size == 0) size = 45;
      }
      return clamp((int) Math.ceil(size / 9.0) + 1, 2, 6);
    }
    return 6;
  }

  public int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  private Set<T> applySearch(Set<T> dataSet) {
    String search = menuData.getSearch();
    if (search == null || search.isEmpty()) {
      return dataSet;
    }
    return dataSet.stream()
        .filter(data -> data.getId().toLowerCase().contains(search.toLowerCase()))
        .collect(Collectors.toSet());
  }

  private LinkedHashSet<T> applyDirection(LinkedHashSet<T> data) {
    if (menuData.getDirection() == MenuData.Direction.DESCENDING) {
      List<T> list = new ArrayList<>(data);
      Collections.reverse(list);
      return new LinkedHashSet<>(list);
    }
    return data;
  }

  private LinkedHashSet<T> applyPage(Set<T> data) {
    int maxPage = getMaxPage(); // Calculate once to be efficient.
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

  @Override
  public void decorate(Player player) {
    // Top control bar background
    ItemStack glass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int i = 0; i < 9; i++) {
      getInventory().setItem(i, glass);
    }

    // Process and display data
    Set<T> filtered = applySearch(dataSet);
    LinkedHashSet<T> ordered = applyOrder(filtered, menuData);
    LinkedHashSet<T> directionApplied = applyDirection(ordered);
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
    if (getOrderOptions() != null && getOrderOptions().length > 0) {
      addButton(2, OrderButton());
      addButton(3, DirectionButton());
    }
    addButton(4, SearchButton());
    if (hasCreateButton()) {
      addButton(5, CreateButton());
    }
    // Check against the current page to show the next button
    if (menuData.getPage() < getMaxPage()) {
      addButton(8, NextButton());
    }

    super.decorate(player);
  }

  public abstract BasicMenu<T> getNewInstance(MenuData menuData, Set<T> dataSet);

  public abstract String getInventoryName();

  public abstract Consumer<InventoryClickEvent> backButton();

  public abstract boolean hasCreateButton();

  public abstract Runnable createNewData(
      String textEntry, InventoryClickEvent event, MenuData menuData);

  public abstract ItemStack getDataButtonDisplayStack(T data, Player player);

  public abstract Consumer<InventoryClickEvent> dataButtonClickConsumer(T data, MenuData menuData);

  public abstract Sound getDenySound();

  public abstract Enum<? extends IDisplayName>[] getOrderOptions();

  public abstract LinkedHashSet<T> applyOrder(Set<T> data, MenuData menuData);

  private InventoryButton DataButton(T data) {
    return new InventoryButton()
        .creator(player -> getDataButtonDisplayStack(data, player))
        .consumer(dataButtonClickConsumer(data, menuData));
  }

  private InventoryButton OrderButton() {
    Enum<? extends IDisplayName> currentOrder = menuData.getOrder();
    List<String> middleLore = new ArrayList<>();
    for (Enum<? extends IDisplayName> orderOption : getOrderOptions()) {
      String displayName = ((IDisplayName) orderOption).getDisplayName();
      if (orderOption == currentOrder) {
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
              menuData.cycleOrder(getOrderOptions());
              player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
              GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
            });
  }

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
              menuData.toggleDirection();
              player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
              GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
            });
  }

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
                          textEntry = textEntry.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
                          createNewData(textEntry, event, menuData).run();
                          return Collections.emptyList();
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  private InventoryButton SearchButton() {
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
    return new InventoryButton()
        .creator(player -> builder.build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 0.75f);
              if (menuData.getSearch() != null && !menuData.getSearch().isBlank()) {
                menuData.setSearch("");
                menuData.setPage(1);
                GUIManager.getInstance().openGUI(getNewInstance(menuData, dataSet), player);
                return;
              }
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Search")
                  .itemLeft(ItemStackHelper.builder(Material.PAPER).name(" ").build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText().trim();
                          menuData.setSearch(textEntry);
                          menuData.setPage(1);
                          GUIManager.getInstance()
                              .openGUI(getNewInstance(menuData, dataSet), player);
                          player.playSound(
                              player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 1f);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

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

  public interface IDisplayName {
    String getDisplayName();
  }
}
