package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.InventoryButton;
import me.mortaldev.jbcrates.modules.menu.InventoryGUI;
import me.mortaldev.jbcrates.utils.ItemStackBuilder;
import me.mortaldev.jbcrates.utils.NBTAPI;
import me.mortaldev.jbcrates.utils.TextUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;

public class MoreOptionsRewardMenu extends InventoryGUI {

  private static final String COMMAND_REWARD_KEY = "commandReward";
  private final Crate crate;
  private Map.Entry<ItemStack, Double> rewardEntry;

  public MoreOptionsRewardMenu(Crate crate, Map.Entry<ItemStack, Double> rewardEntry) {
    this.crate = crate;
    this.rewardEntry = rewardEntry;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("&3&lMore Reward Options"));
  }

  @Override
  public void decorate(Player player) {
    addButton(18, backButton());

    ItemStack rewardItemStack =
        CrateManager.generateDisplayRewardItemStack(
            rewardEntry.getKey(),
            rewardEntry.getValue(),
            crate.getRewardDisplay(rewardEntry.getKey()));

    this.getInventory().setItem(13, rewardItemStack);

    addButton(16, removeButton());
    if (NBTAPI.hasNBT(rewardEntry.getKey(), COMMAND_REWARD_KEY)) {
      addButton(10, commandItemButton());
    } else {
      addButton(10, normalItemButton());
    }
    super.decorate(player);
  }

  private InventoryButton normalItemButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.CONDUIT)
                    .name("&3&lCommand Item")
                    .addLore("&7This will run a command as a console")
                    .addLore("&7when a player earns it in a crate.")
                    .addLore("&7Use %player% for a player placeholder.")
                    .addLore("&7")
                    .addLore("&e[Click to convert]")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              enterRewardCommand(player);
            });
  }

  private void enterRewardCommand(Player player) {
    String cmd = NBTAPI.getNBT(rewardEntry.getKey(), COMMAND_REWARD_KEY) != null ? NBTAPI.getNBT(rewardEntry.getKey(), COMMAND_REWARD_KEY) : "";
    ItemStackBuilder rewardItem = ItemStackBuilder.builder(Material.CONDUIT)
        .name(cmd);
    new AnvilGUI.Builder()
        .plugin(Main.getInstance())
        .title("Reward Command: ")
        .itemLeft(rewardItem.build())
        .onClick(
            (slot, stateSnapshot) -> {
              if (slot == 2) {
                String textEntry = stateSnapshot.getText();
                ItemStack clonedItem = rewardEntry.getKey().clone();
                clonedItem.setAmount(1);
                NBTAPI.addNBT(clonedItem, COMMAND_REWARD_KEY, textEntry);

                crate.updateReward(rewardEntry.getKey(), clonedItem);
                CrateManager.updateCrate(crate.getId(), crate);

                rewardEntry = Map.entry(clonedItem, rewardEntry.getValue());

                Main.getGuiManager().openGUI(new MoreOptionsRewardMenu(crate, rewardEntry), player);
              }
              return Collections.emptyList();
            })
        .open(player);
  }

  private InventoryButton commandItemButton() {
    return new InventoryButton()
        .creator(
            player -> {
              String commandReward = NBTAPI.getNBT(rewardEntry.getKey(), COMMAND_REWARD_KEY);
              if (!commandReward.startsWith("/")) {
                commandReward = "/" + commandReward;
              }
              return ItemStackBuilder.builder(Material.CONDUIT)
                  .name("&3&lCommand Item")
                  .addLore("&7This item is currently a command item.")
                  .addLore("&7")
                  .addLore("&3Current: " + commandReward)
                  .addLore("&7")
                  .addLore("&e[Left Click to change command]")
                  .addLore("&e[Right Click to convert to normal item]")
                  .build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (event.isRightClick()) {

                ItemStack clonedItem = rewardEntry.getKey().clone();
                NBTAPI.removeNBT(clonedItem, COMMAND_REWARD_KEY);
                crate.updateReward(rewardEntry.getKey(), clonedItem);

                rewardEntry = Map.entry(clonedItem, rewardEntry.getValue());

                Main.getGuiManager().openGUI(new MoreOptionsRewardMenu(crate, rewardEntry), player);
              } else if (event.isLeftClick()) {
                enterRewardCommand(player);
              }
            });
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
              Main.getGuiManager().openGUI(new CrateRewardsMenu(crate), player);
            });
  }

  private InventoryButton removeButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.RED_DYE)
                    .name("&c&lRemove")
                    .addLore("&7Removes this reward from the crate.")
                    .addLore("&7")
                    .addLore("&e[Click to remove")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              Main.getGuiManager().openGUI(new RemoveCrateRewardMenu(crate, rewardEntry), player);
            });
  }
}
