package me.mortaldev.jbcrates.menus.edit;

import java.math.BigDecimal;
import java.util.*;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.listeners.ChatListener;
import me.mortaldev.jbcrates.menus.MenuData;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateHandler;
import me.mortaldev.jbcrates.modules.crate.CrateItem;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.records.Pair;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CrateRewardsMenu extends InventoryGUI implements Listener {
  private final Crate crate;
  private final MenuData menuData;

  public CrateRewardsMenu(Crate crate, MenuData menuData) {
    this.crate = crate;
    this.menuData = menuData;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, inventorySize() * 9, TextUtil.format("&3&lCrate Rewards"));
  }

  private int inventorySize() {
    double size = ((double) crate.getRewardsMap().size() / 9) + 1;
    return Utils.clamp((int) Math.ceil(size), 3, 6);
  }

  @Override
  public void decorate(Player player) {
    int i = 9;
    LinkedHashMap<CrateItem, BigDecimal> table = crate.getRewardsMap().getTable();
    for (CrateItem crateItem : crate.getDisplaySet()) {
      this.addButton(i, rewardButton(crateItem, table.get(crateItem)));
      i++;
    }
    ItemStack whiteGlass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).emptyName().build();
    for (int j = 0; j < 9; j++) {
      getInventory().setItem(j, whiteGlass);
    }
    addButton(0, backButton());
    addButton(2, balanceRewardChancesButton());
    addButton(4, addRewardButton());
    addButton(6, adjustAmountToWin());
    addButton(8, sortRewardsButton());
    super.allowBottomInventoryClick(true);
    super.decorate(player);
  }

  private InventoryButton sortRewardsButton() {
    return new InventoryButton()
        .creator(
            player -> {
              ItemStackHelper.Builder builder =
                  ItemStackHelper.builder(Material.BOOK)
                      .name("&3&lSort Rewards")
                      .addLore(" ")
                      .addLore("&3&lSort By:");
              for (CrateManager.SortBy value : CrateManager.SortBy.values()) {
                if (value == crate.getSortBy()) {
                  builder.addLore("&f - " + value.getName());
                } else {
                  builder.addLore("&7 - " + value.getName());
                }
              }
              builder.addLore("");
              if (crate.getOrder().equals(CrateManager.Order.DESCENDING)) {
                builder.addLore("&7Ascending &f\\ &f&lDescending");
              } else {
                builder.addLore("&f&lAscending &f\\ &7Descending");
              }
              builder.addLore("");
              builder.addLore("&e[Left-Click to scroll sort]");
              builder.addLore("&e[Right-Click to change order]");
              return builder.build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (event.getClick() == ClickType.RIGHT) {
                CrateManager.Order order = CrateManager.Order.flip(crate.getOrder());
                crate.setOrder(order);
              } else if (event.getClick() == ClickType.LEFT) {
                CrateManager.SortBy next = CrateManager.SortBy.next(crate.getSortBy());
                crate.setSortBy(next);
              }
              Crate newCrate = CrateHandler.getInstance().sortRewards(crate);
              CrateManager.getInstance().update(newCrate);
              GUIManager.getInstance().openGUI(new CrateRewardsMenu(newCrate, menuData), player);
            });
  }

  private InventoryButton balanceRewardChancesButton() {
    return new InventoryButton()
        .creator(
            player -> {
              BigDecimal total = crate.getRewardsMap().getTotal();
              return ItemStackHelper.builder(Material.REDSTONE)
                  .name("&3&lBalance Reward Chances")
                  .addLore("&7Will balance the reward chances")
                  .addLore("&7to make sure it adds up to 100%")
                  .addLore("&7")
                  .addLore("&3Current: " + total.toPlainString() + "%")
                  .addLore("&7")
                  .addLore("&e[Click to balance chances]")
                  .build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              crate.getRewardsMap().balanceTable();
              crate.modify();
              CrateManager.getInstance().update(crate);
              GUIManager.getInstance().openGUI(new CrateRewardsMenu(crate, menuData), player);
            });
  }

  private InventoryButton adjustAmountToWin() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.PAPER)
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
                      ItemStackHelper.builder(Material.PAPER)
                          .name(String.valueOf(crate.getAmountToWin()))
                          .build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String textEntry = stateSnapshot.getText().trim();
                          try {
                            int amount = Integer.parseInt(textEntry);
                            if (amount < 1) {
                              player.sendMessage(TextUtil.format("&cMust be 1 or greater!"));
                              GUIManager.getInstance()
                                  .openGUI(new CrateRewardsMenu(crate, menuData), player);
                              return Collections.emptyList();
                            }
                            crate.setAmountToWin(amount);
                            CrateManager.getInstance().update(crate);
                            GUIManager.getInstance()
                                .openGUI(new CrateRewardsMenu(crate, menuData), player);
                          } catch (NumberFormatException e) {
                            player.sendMessage(TextUtil.format("&cPlease enter a valid number"));
                          }
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
                ItemStackHelper.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to previous page")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              GUIManager.getInstance().openGUI(new ManageCrateMenu(crate, menuData), player);
            });
  }

  private InventoryButton addRewardButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.BUCKET)
                    .name("&2&lAdd Reward")
                    .addLore("&7Click with item in hand to add.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ItemStack cursor = player.getItemOnCursor();
              if (cursor.getType() == Material.AIR) {
                return;
              }
              ItemStack cursorClone = cursor.clone();
              CrateItem crateItem = new CrateItem(cursorClone);
              List<CrateItem> displaySet = crate.getDisplaySet();
              for (CrateItem item : displaySet) {
                if (item.getItemStack().equals(cursorClone)) {
                  player.sendMessage(TextUtil.format("&cAlready exists in this crate."));
                  return;
                }
              }
              displaySet.add(crateItem);
              crate.getRewardsMap().put(crateItem, true);
              crate.modify();
              CrateManager.getInstance().update(crate);
              GUIManager.getInstance().openGUI(new CrateRewardsMenu(crate, menuData), player);
            });
  }

  private InventoryButton rewardButton(CrateItem reward, BigDecimal chance) {
    return new InventoryButton()
        .creator(
            player -> CrateHandler.getInstance().generateEditingRewardItemStack(reward, chance))
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (event.getClick() == ClickType.RIGHT) {
                GUIManager.getInstance()
                    .openGUI(
                        new MoreOptionsRewardMenu(crate, new Pair<>(reward, chance), menuData),
                        player);
              } else if (event.getClick() == ClickType.LEFT) { // Change reward chance
                new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Reward Chance")
                    .itemLeft(
                        ItemStackHelper.builder(Material.FLOWER_BANNER_PATTERN)
                            .name(chance.toPlainString())
                            .build())
                    .onClick(
                        (slot, stateSnapshot) -> {
                          if (slot == 2) {
                            String textEntry = stateSnapshot.getText().trim();
                            try {
                              double parsedDouble = Double.parseDouble(textEntry);
                              crate.getRewardsMap().updateKey(reward, parsedDouble);
                              crate.modify();
                              CrateManager.getInstance().update(crate);
                              GUIManager.getInstance()
                                  .openGUI(new CrateRewardsMenu(crate, menuData), player);
                            } catch (NumberFormatException e) {
                              player.sendMessage(TextUtil.format("&cPlease enter a valid number"));
                            }
                          }
                          return Collections.emptyList();
                        })
                    .open(player);
              } else if (event.getClick() == ClickType.MIDDLE) { // Change reward name
                player.getInventory().close();
                player.sendMessage("");
                String originalName = reward.getDisplayText();
                Component component =
                    MiniMessage.miniMessage()
                        .deserialize(
                            "<hover:show_text:'<gray>Click for Original Text'><click:SUGGEST_COMMAND:'"
                                + originalName
                                + "'>[Original]</click></hover>")
                        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                player.sendMessage(
                    TextUtil.format("&7(Lasts 20s) &3Enter the new name for the reward: ")
                        .append(component));
                player.sendMessage("");
                ChatListener.makeRequest(
                    player,
                    20 * 20L,
                    (chatComponent) -> {
                      String string = TextUtil.chatComponentToString(chatComponent);
                      CrateItem clonedReward = reward.clone();
                      reward.setDisplayText(string);
                      crate.getRewardsMap().updateKey(clonedReward, reward);
                      crate.modify();
                      CrateManager.getInstance().update(crate);
                      GUIManager.getInstance()
                          .openGUI(new CrateRewardsMenu(crate, menuData), player);
                    });
              } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                List<CrateItem> crateItems = Utils.moveElement(crate.getDisplaySet(), reward, -1);
                crate.setDisplaySet(crateItems);
                crate.setSortBy(CrateManager.SortBy.CUSTOM);
                crate.modify();
                CrateManager.getInstance().update(crate);
                GUIManager.getInstance().openGUI(new CrateRewardsMenu(crate, menuData), player);
              } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                List<CrateItem> crateItems = Utils.moveElement(crate.getDisplaySet(), reward, +1);
                crate.setDisplaySet(crateItems);
                crate.setSortBy(CrateManager.SortBy.CUSTOM);
                crate.modify();
                CrateManager.getInstance().update(crate);
                GUIManager.getInstance().openGUI(new CrateRewardsMenu(crate, menuData), player);
              }
            });
  }
}
