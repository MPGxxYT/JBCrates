package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.InventoryButton;
import me.mortaldev.jbcrates.modules.menu.InventoryGUI;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ConfirmDeleteCrateMenu extends InventoryGUI {

  private final Crate crate;

  public ConfirmDeleteCrateMenu(Crate crateToDelete) {
    this.crate = crateToDelete;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("&c&lDelete Crate"));
  }

  @Override
  public void decorate(Player player) {

    this.addButton(9, cancelButton());
    this.addButton(10, cancelButton());
    this.addButton(11, cancelButton());

    this.getInventory().setItem(13, CrateManager.generateDisplayCrateItemStack(crate));

    this.addButton(15, confirmButton());
    this.addButton(16, confirmButton());
    this.addButton(17, confirmButton());
    super.decorate(player);
  }

  private InventoryButton confirmButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.LIME_STAINED_GLASS_PANE)
                    .name("&2&lYES! DELETE '" + crate.getId() + "'")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              CrateManager.removeCrate(crate);
              player.sendMessage(TextUtil.format("&fDeleted crate &3'" + crate.getId() + "'&f."));
              Main.getGuiManager().openGUI(new ViewCratesMenu(1), player);
            });
  }

  private InventoryButton cancelButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.RED_STAINED_GLASS_PANE)
                    .name("&c&lNO! DONT DELETE '" + crate.getId() + "'")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              Main.getGuiManager().openGUI(new ManageCrateMenu(crate), player);
            });
  }
}
