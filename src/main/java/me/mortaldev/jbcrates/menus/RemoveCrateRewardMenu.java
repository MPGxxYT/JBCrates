package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.InventoryButton;
import me.mortaldev.jbcrates.modules.menu.InventoryGUI;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class RemoveCrateRewardMenu extends InventoryGUI {

  private final Crate crate;
  private final Map.Entry<ItemStack, Double> reward;

  public RemoveCrateRewardMenu(Crate crate, Map.Entry<ItemStack, Double> reward) {
    this.crate = crate;
    this.reward = reward;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("&c&lRemove Crate Reward"));
  }

  @Override
  public void decorate(Player player) {

    this.addButton(9, cancelButton());
    this.addButton(10, cancelButton());
    this.addButton(11, cancelButton());

    this.getInventory()
        .setItem(
            13,
            CrateManager.generateDisplayRewardItemStack(
                reward.getKey(), reward.getValue(), crate.getRewardDisplay(reward.getKey())));

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
                    .name("&2&lYES! REMOVE '" + Utils.itemName(reward.getKey()) + "'")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              crate.removeReward(reward.getKey());
              CrateManager.balanceRewardChances(crate);
              CrateManager.updateCrate(crate.getId(), crate);
              Main.getGuiManager().openGUI(new CrateRewardsMenu(crate, crate.getOrder()), player);
            });
  }

  private InventoryButton cancelButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.RED_STAINED_GLASS_PANE)
                    .name("&c&lNO! DONT REMOVE '" + Utils.itemName(reward.getKey()) + "'")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              Main.getGuiManager().openGUI(new MoreOptionsRewardMenu(crate, reward), player);
            });
  }
}
