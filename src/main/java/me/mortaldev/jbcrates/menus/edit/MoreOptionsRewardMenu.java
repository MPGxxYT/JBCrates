package me.mortaldev.jbcrates.menus.edit;

import java.math.BigDecimal;
import java.util.Collections;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.menus.MenuData;
import me.mortaldev.jbcrates.menus.factories.ConfirmMenu;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateHandler;
import me.mortaldev.jbcrates.modules.crate.CrateItem;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.records.Pair;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.NBTAPI;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MoreOptionsRewardMenu extends InventoryGUI {

  public static final String COMMAND_REWARD_KEY = "commandReward";
  private final Crate crate;
  private final Pair<CrateItem, BigDecimal> reward;
  private final MenuData menuData;

  public MoreOptionsRewardMenu(Crate crate, Pair<CrateItem, BigDecimal> reward, MenuData menuData) {
    this.crate = crate;
    this.reward = reward;
    this.menuData = menuData;
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, TextUtil.format("&3&lMore Reward Options"));
  }

  @Override
  public void decorate(Player player) {
    addButton(18, backButton());

    ItemStack rewardItemStack =
        CrateHandler.getInstance().generateDisplayRewardItemStack(reward.first(), reward.second());
    this.getInventory().setItem(13, rewardItemStack);

    addButton(16, removeButton());
    if (NBTAPI.hasNBT(reward.first().getItemStack(), COMMAND_REWARD_KEY)) {
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
                ItemStackHelper.builder(Material.CONDUIT)
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
    CrateItem crateItem = reward.first();
    ItemStack itemStack = crateItem.getItemStack();
    String cmd =
        NBTAPI.getNBT(itemStack, COMMAND_REWARD_KEY) != null
            ? NBTAPI.getNBT(itemStack, COMMAND_REWARD_KEY)
            : "";
    ItemStackHelper.Builder rewardItem = ItemStackHelper.builder(Material.CONDUIT).name(cmd);
    new AnvilGUI.Builder()
        .plugin(Main.getInstance())
        .title("Reward Command: ")
        .itemLeft(rewardItem.build())
        .onClick(
            (slot, stateSnapshot) -> {
              if (slot == 2) {
                String textEntry = stateSnapshot.getText();
                ItemStack clonedItem = itemStack.clone();
                clonedItem.setAmount(1);
                NBTAPI.addNBT(clonedItem, COMMAND_REWARD_KEY, textEntry);
                CrateItem clonedCrateItem = updateCrateItem(crateItem, clonedItem);
                CrateManager.getInstance().update(crate);
                GUIManager.getInstance()
                    .openGUI(
                        new MoreOptionsRewardMenu(
                            crate, new Pair<>(clonedCrateItem, reward.second()), menuData),
                        player);
              }
              return Collections.emptyList();
            })
        .open(player);
  }

  public CrateItem updateCrateItem(CrateItem originalCrateItem, ItemStack itemStack) {
    CrateItem clonedCrateItem = originalCrateItem.clone();
    clonedCrateItem.setItemStack(itemStack);

    int i = crate.getDisplaySet().indexOf(originalCrateItem);
    crate.getDisplaySet().set(i, clonedCrateItem);

    crate.getRewardsMap().updateKey(originalCrateItem, clonedCrateItem);
    crate.modify();
    return clonedCrateItem;
  }

  public void removeCrateItem(CrateItem crateItem) {
    crate.getDisplaySet().remove(crateItem);
    crate.getRewardsMap().remove(crateItem, true);
    crate.modify();
  }

  private InventoryButton commandItemButton() {
    CrateItem crateItem = reward.first();
    ItemStack itemStack = crateItem.getItemStack();
    return new InventoryButton()
        .creator(
            player -> {
              String commandReward = NBTAPI.getNBT(itemStack, COMMAND_REWARD_KEY);
              if (commandReward == null) {
                commandReward = "/";
              } else if (!commandReward.startsWith("/")) {
                commandReward = "/" + commandReward;
              }
              return ItemStackHelper.builder(Material.CONDUIT)
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

                ItemStack clonedItem = itemStack.clone();
                NBTAPI.removeNBT(clonedItem, COMMAND_REWARD_KEY);
                CrateItem clonedCrateItem = updateCrateItem(crateItem, clonedItem);
                CrateManager.getInstance().update(crate);
                GUIManager.getInstance()
                    .openGUI(
                        new MoreOptionsRewardMenu(
                            crate, new Pair<>(clonedCrateItem, reward.second()), menuData),
                        player);
              } else if (event.isLeftClick()) {
                enterRewardCommand(player);
              }
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
              GUIManager.getInstance().openGUI(new CrateRewardsMenu(crate, menuData), player);
            });
  }

  private InventoryButton removeButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.RED_DYE)
                    .name("&c&lRemove")
                    .addLore("&7Removes this reward from the crate.")
                    .addLore("&7")
                    .addLore("&e[Click to remove]")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ConfirmMenu confirmMenu =
                  new ConfirmMenu(
                      "&cRemove Reward?",
                      reward.first().getItemStack().clone(),
                      (player1) -> {
                        removeCrateItem(reward.first());
                        CrateManager.getInstance().update(crate);
                        GUIManager.getInstance()
                            .openGUI(new CrateRewardsMenu(crate, menuData), player);
                      },
                      (player1) -> GUIManager.getInstance()
                          .openGUI(new MoreOptionsRewardMenu(crate, reward, menuData), player));
              GUIManager.getInstance().openGUI(confirmMenu, player);
            });
  }
}
