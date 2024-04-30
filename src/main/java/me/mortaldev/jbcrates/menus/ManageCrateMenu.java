package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.InventoryButton;
import me.mortaldev.jbcrates.modules.menu.InventoryGUI;
import me.mortaldev.jbcrates.utils.ItemStackBuilder;
import me.mortaldev.jbcrates.utils.TextUtil;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.List;

public class ManageCrateMenu extends InventoryGUI {

  private final Crate crate;

  public ManageCrateMenu(Crate crate) {
    this.crate = crate;
  }

  @Override
  protected Inventory createInventory() {
    if (crate == null){
      return Bukkit.createInventory(
          null, 3 * 9, TextUtil.format("&3&lEditing Crate: &f"));
    } else {
      return Bukkit.createInventory(
          null, 3 * 9, TextUtil.format("&3&lEditing Crate: &f" + crate.getId()));
    }
  }

  @Override
  public void decorate(Player player) {

    this.addButton(10, nameButton());
    this.addButton(12, descriptionButton());
    this.addButton(14, rewardsButton());
    this.addButton(16, deleteButton());
    this.addButton(18, backButton());
    super.decorate(player);
  }

  private InventoryButton backButton() {
    return new InventoryButton()
        .creator(
            playerCreator ->
                ItemStackBuilder.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to previous page")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              Main.getGuiManager().openGUI(new ViewCratesMenu(1), player);
            });
  }

  private InventoryButton deleteButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.RED_DYE)
                    .name("&c&lDELETE:")
                    .addLore("&7")
                    .addLore("&cDelete crate &l" + crate.getId() + "&c?")
                    .addLore("&7")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              Main.getGuiManager().openGUI(new ConfirmDeleteCrateMenu(crate), player);
            });
  }

  private InventoryButton rewardsButton() {
    return new InventoryButton()
        .creator(
            player -> {
              List<Component> crateRewardsText = CrateManager.getCrateRewardsText(crate, 7);
              return ItemStackBuilder.builder(Material.CHEST)
                  .name("&3&lREWARDS:")
                  .addLore("&7")
                  .addLore(crateRewardsText)
                  .addLore("&7")
                  .addLore("&e[EDIT]")
                  .build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              Main.getGuiManager().openGUI(new CrateRewardsMenu(crate), player);
            });
  }

  private InventoryButton descriptionButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.PAPER)
                    .name("&3&lDESCRIPTION:")
                    .addLore("&7")
                    .addLore(crate.getDescription())
                    .addLore("&7")
                    .addLore("&e[CHANGE]")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Crate Desc: " + crate.getId())
                  .itemLeft(
                      ItemStackBuilder.builder(Material.PAPER).name(crate.getDescription()).build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          crate.setDescription(stateSnapshot.getText());
                          CrateManager.updateCrate(crate.getId(), crate);
                          Main.getGuiManager().openGUI(new ManageCrateMenu(crate), player);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  private InventoryButton nameButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.NAME_TAG)
                    .name("&3&lNAME:")
                    .addLore("&7")
                    .addLore(crate.getDisplayName())
                    .addLore("&7")
                    .addLore("&e[RENAME]")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Rename Crate: " + crate.getId())
                  .itemLeft(
                      ItemStackBuilder.builder(Material.NAME_TAG)
                          .name(crate.getDisplayName())
                          .build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          crate.setDisplayName(stateSnapshot.getText());
                          CrateManager.updateCrate(crate.getId(), crate);
                          Main.getGuiManager().openGUI(new ManageCrateMenu(crate), player);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }
}
