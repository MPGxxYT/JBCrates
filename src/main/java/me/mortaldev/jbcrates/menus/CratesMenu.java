package me.mortaldev.jbcrates.menus;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import me.mortaldev.jbcrates.menus.basicmenu.BasicMenu;
import me.mortaldev.jbcrates.menus.basicmenu.EnumFilter;
import me.mortaldev.jbcrates.menus.basicmenu.MenuData;
import me.mortaldev.jbcrates.menus.basicmenu.OrderOption;
import me.mortaldev.jbcrates.menus.edit.ManageCrateMenu;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateHandler;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CratesMenu extends BasicMenu<Crate> implements BasicMenu.Identifiable {

  public CratesMenu(MenuData menuData) {
    super(menuData, CrateManager.getInstance().getSet());
  }

  @Override
  public BasicMenu<Crate> getNewInstance(MenuData menuData, Set<Crate> dataSet) {
    return new CratesMenu(menuData);
  }

  @Override
  public String getInventoryName() {
    return "Crates";
  }

  @Override
  public Consumer<InventoryClickEvent> backButton() {
    return null;
  }

  @Override
  public boolean hasCreateButton() {
    return true;
  }

  @Override
  public Runnable createNewData(String textEntry, InventoryClickEvent event, MenuData menuData) {
    return () -> {
      Player player = (Player) event.getWhoClicked();
      player.closeInventory();
      CrateManager.getInstance()
          .getByID(textEntry)
          .ifPresentOrElse(
              (c) -> {
                player.sendMessage("&cCrate by that ID already exists.");
                GUIManager.getInstance().openGUI(new CratesMenu(menuData), player);
              },
              () -> {
                Crate crate = Crate.create(textEntry);
                crate.setDisplayName(textEntry);
                CrateManager.getInstance().add(crate);
                menuData.setDirection(MenuData.Direction.DESCENDING);
                GUIManager.getInstance().openGUI(new ManageCrateMenu(crate, menuData), player);
              });
    };
  }

  @Override
  public ItemStack getDataButtonDisplayStack(Crate data, Player player) {
    return CrateHandler.getInstance().generateDisplayCrateItemStack(data);
  }

  @Override
  public Consumer<InventoryClickEvent> dataButtonClickConsumer(Crate data, MenuData menuData) {
    return (event) -> {
      Player player = (Player) event.getWhoClicked();
      GUIManager.getInstance().openGUI(new ManageCrateMenu(data, menuData), player);
    };
  }

  @Override
  public Sound getDenySound() {
    return Sound.BLOCK_NOTE_BLOCK_BASS;
  }

  @Override
  public List<OrderOption<Crate>> getOrderOptions() {
    return List.of(
        new OrderOption<>(
            "alphabetical",
            "Alphabetical",
            Comparator.comparing(Crate::getId, String.CASE_INSENSITIVE_ORDER)),
        new OrderOption<>(
            "date_created", "Date Created", Comparator.comparing(Crate::getDateCreated).reversed()),
        new OrderOption<>(
            "last_modified", "Last Modified", Comparator.comparing(Crate::getLastModified).reversed()));
  }

  @Override
  public List<EnumFilter<Crate, ?>> getEnumFilters() {
    return List.of();
  }

  @Override
  public String getId() {
    return "crates_menu";
  }
}
