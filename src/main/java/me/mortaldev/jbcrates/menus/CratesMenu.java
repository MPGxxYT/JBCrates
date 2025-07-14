package me.mortaldev.jbcrates.menus;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.mortaldev.jbcrates.menus.edit.ManageCrateMenu;
import me.mortaldev.jbcrates.menus.factories.BasicMenu;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateHandler;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CratesMenu extends BasicMenu<Crate> {

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
                menuData.setOrder(MenuData.Order.LAST_MODIFIED);
                menuData.setDirection(MenuData.Direction.DESCENDING);
                GUIManager.getInstance().openGUI(new ManageCrateMenu(crate, menuData), player);
              });
    };
  }

  @Override
  public boolean hasCreateButton() {
    return true;
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
  public Enum<? extends IDisplayName>[] getOrderOptions() {
    return MenuData.Order.values();
  }

  @Override
  public LinkedHashSet<Crate> applyOrder(Set<Crate> data, MenuData menuData) {
    if (menuData.getOrder() instanceof MenuData.Order order) {
      return switch (order) {
        case ALPHABET ->
            data.stream()
                .sorted(Comparator.comparing(Crate::getId, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        case DATE_CREATED ->
            data.stream()
                .sorted(Comparator.comparing(Crate::getDateCreated))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        case LAST_MODIFIED ->
            data.stream()
                .sorted(Comparator.comparing(Crate::getLastModified))
                .collect(Collectors.toCollection(LinkedHashSet::new));
      };
    }
    return new LinkedHashSet<>(data);
  }
}
