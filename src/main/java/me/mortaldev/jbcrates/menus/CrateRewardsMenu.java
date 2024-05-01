package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.InventoryButton;
import me.mortaldev.jbcrates.modules.menu.InventoryGUI;
import me.mortaldev.jbcrates.utils.ItemStackBuilder;
import me.mortaldev.jbcrates.utils.TextUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

public class CrateRewardsMenu extends InventoryGUI {

  private final Crate crate;

  public CrateRewardsMenu(Crate crate) {
    this.crate = crate;
  }

  @Override
  protected Inventory createInventory() {
    // will have it scale based on amount of rewards
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("&3&lCrate Rewards"));
  }

  @Override
  public void decorate(Player player) {
    int i = 0;
    for (Map.Entry<ItemStack, Double> entry : crate.getRewardsMap().entrySet()) {
      this.addButton(i, rewardButton(entry));
      i++;
    }
    ItemStack whiteGlass =
        ItemStackBuilder.builder(Material.WHITE_STAINED_GLASS_PANE).name("&7").build();
    this.getInventory().setItem(19, whiteGlass);
    this.getInventory().setItem(20, whiteGlass);
    this.getInventory().setItem(21, whiteGlass);
    this.getInventory().setItem(23, whiteGlass);
    this.getInventory().setItem(24, whiteGlass);
    this.getInventory().setItem(25, whiteGlass);
    this.getInventory().setItem(26, whiteGlass);
    addButton(24, adjustAmountToWin());
    addButton(22, addRewardButton());
    addButton(20, balanceRewardChancesButton());
    addButton(18, backButton());
    super.allowBottomInventoryClick(true);
    super.decorate(player);
  }

  private InventoryButton balanceRewardChancesButton() {
    return new InventoryButton()
        .creator(
            player ->{
              Double total = CrateManager.getRewardChancesTotal(crate);
              return ItemStackBuilder.builder(Material.REDSTONE)
                  .name("&3&lBalance Reward Chances")
                  .addLore("&7Will balance the reward chances")
                  .addLore("&7to make sure it adds up to 100%")
                  .addLore("&7")
                  .addLore("&3Current: " + total + "%")
                  .addLore("&7")
                  .addLore("&e[Click to balance chances]")
                  .build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              CrateManager.balanceRewardChances(crate);
              CrateManager.updateCrate(crate.getId(), crate);
              Main.getGuiManager().openGUI(new CrateRewardsMenu(crate), player);
            });
  }

  private InventoryButton adjustAmountToWin() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.PAPER)
                    .name("&3&lAmount To Win")
                    .addLore("&7The amount of items in this crate")
                    .addLore("&7that you want the player to receive.")
                    .addLore("")
                    .addLore("&3" + crate.getAmountToWin() + " Reward(s)")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Amount of Rewards to Win")
                  .itemLeft(
                      ItemStackBuilder.builder(Material.PAPER)
                          .name(String.valueOf(crate.getAmountToWin()))
                          .build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          Integer newValue =
                              Integer.valueOf(stateSnapshot.getText().replaceAll("[^0-9]", ""));
                          crate.setAmountToWin(newValue);
                          CrateManager.updateCrate(crate.getId(), crate);
                          Main.getGuiManager().openGUI(new CrateRewardsMenu(crate), player);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
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
              Main.getGuiManager().openGUI(new ManageCrateMenu(crate), player);
            });
  }

  private InventoryButton addRewardButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.BUCKET)
                    .name("&2&lAdd Reward")
                    .addLore("&7Click with item in hand to add.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (player.getItemOnCursor().getType() == Material.AIR) {
                return;
              }
              BigDecimal prob;
              if (crate.getRewardsMap().values().isEmpty()){
                prob = new BigDecimal(100);
              } else {
                BigDecimal size = new BigDecimal(crate.getRewardsMap().values().size());
                //size = size.add(new BigDecimal(1));
                BigDecimal hundred = new BigDecimal(100);
                prob = hundred.divide(size, 2, RoundingMode.HALF_UP);
              }
              crate.addReward(player.getItemOnCursor(), prob.doubleValue());
              CrateManager.balanceRewardChances(crate);
              CrateManager.updateCrate(crate.getId(), crate);
              Main.getGuiManager().openGUI(new CrateRewardsMenu(crate), player);
            });
  }

  private InventoryButton rewardButton(Map.Entry<ItemStack, Double> reward) {
    return new InventoryButton()
        .creator(player -> CrateManager.generateRewardItemStack(reward.getKey().clone(), reward.getValue(), crate.getRewardsDisplayMap().get(reward.getKey())))
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (event.getClick() == ClickType.RIGHT) {
                Main.getGuiManager().openGUI(new RemoveCrateRewardMenu(crate, reward), player);
              } else if (event.getClick() == ClickType.LEFT) {
                new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Reward Chance")
                    .itemLeft(
                        ItemStackBuilder.builder(Material.FLOWER_BANNER_PATTERN)
                            .name(String.valueOf(reward.getValue()))
                            .build())
                    .onClick(
                        (slot, stateSnapshot) -> {
                          if (slot == 2) {
                            Double newValue =
                                Double.valueOf(stateSnapshot.getText().replaceAll("[^A+-Z+]", ""));
                            crate.updateReward(reward.getKey(), newValue);
                            CrateManager.updateCrate(crate.getId(), crate);
                            Main.getGuiManager().openGUI(new CrateRewardsMenu(crate), player);
                          }
                          return Collections.emptyList();
                        })
                    .open(player);
              } else if (event.getClick() == ClickType.MIDDLE) {
                new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Reward Display Name")
                    .itemLeft(
                        ItemStackBuilder.builder(Material.FLOWER_BANNER_PATTERN)
                            .name(String.valueOf(crate.getRewardsDisplayMap().get(reward.getKey())))
                            .build())
                    .onClick(
                        (slot, stateSnapshot) -> {
                          if (slot == 2) {
                            String display = stateSnapshot.getText();
                            crate.updateReward(reward.getKey(), display);
                            CrateManager.updateCrate(crate.getId(), crate);
                            Main.getGuiManager().openGUI(new CrateRewardsMenu(crate), player);
                          }
                          return Collections.emptyList();
                        })
                    .open(player);
              }
            });
  }
}
