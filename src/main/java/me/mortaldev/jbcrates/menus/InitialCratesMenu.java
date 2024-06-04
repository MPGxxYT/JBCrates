package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.menu.InventoryButton;
import me.mortaldev.jbcrates.modules.menu.InventoryGUI;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class InitialCratesMenu extends InventoryGUI {

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("&3&lJBCrates"));
  }

  @Override
  public void decorate(Player player) {
    this.addButton(13, chestMenuButton());
    super.decorate(player);
  }

  private InventoryButton chestMenuButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.CHEST)
                    .name("&3&lView Crates")
                    .addLore("&7Click to view crates")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              Main.getGuiManager().openGUI(new ViewCratesMenu(1), player);
            });
  }
}
