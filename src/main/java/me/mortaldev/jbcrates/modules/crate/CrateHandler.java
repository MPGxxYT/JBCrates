package me.mortaldev.jbcrates.modules.crate;

import java.math.BigDecimal;
import java.util.*;
import me.mortaldev.jbcrates.utils.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CrateHandler {

  private static class Singleton {
    private static final CrateHandler INSTANCE = new CrateHandler();
  }

  public static CrateHandler getInstance() {
    return Singleton.INSTANCE;
  }

  private CrateHandler() {}

  public List<String> getCrateRewardsText(Crate crate) {
    return getCrateRewardsText(crate, crate.getRewardsMap().size(), false);
  }

  public List<String> getCrateRewardsText(Crate crate, boolean displayMode) {
    return getCrateRewardsText(crate, crate.getRewardsMap().size(), displayMode);
  }

  public List<String> getCrateRewardsText(Crate crate, int maxItemsListed) {
    return getCrateRewardsText(crate, maxItemsListed, false);
  }

  public List<String> getCrateRewardsText(Crate crate, int maxItemsListed, boolean displayMode) {
    List<String> rewardsText = new ArrayList<>();
    ChanceMap<CrateItem> rewardsMap = crate.getRewardsMap();
    if (rewardsMap.isEmpty()) {
      rewardsText.add("&cNo Rewards Inside.");
    } else {
      int i = 0;
      for (Map.Entry<CrateItem, BigDecimal> entry : rewardsMap.getTable().entrySet()) {
        BigDecimal chance = entry.getValue();
        CrateItem crateItem = entry.getKey();
        if (i <= maxItemsListed) {
          i++;
          ItemStack itemStack = crateItem.getItemStack();
          String displayName = crateItem.getDisplayText();
          if (displayMode) {
            rewardsText.add("&7 - " + displayName + "&r&7 x" + itemStack.getAmount());
          } else {
            rewardsText.add(
                "&7" + displayName + "&r&7 x" + itemStack.getAmount() + " &3" + chance + "% Chance");
          }
        } else {
          break;
        }
      }
      int itemsLeft = rewardsMap.size() - maxItemsListed;
      if (itemsLeft > 0) {
        rewardsText.add("&7..." + itemsLeft + " more");
      }
    }
    return rewardsText;
  }

  public ItemStack generateEditingRewardItemStack(CrateItem crateItem, BigDecimal chance) {
    ItemStack clonedItemStack = crateItem.getItemStack().clone();
    ItemStackHelper.Builder itemStackHelper =
        ItemStackHelper.builder(clonedItemStack)
            .addLore("&7")
            .addLore("&7Display: " + crateItem.getDisplayText())
            .addLore("&7")
            .addLore("&3" + chance + "% Chance")
            .addLore("&7");

    String commandReward = NBTAPI.getNBT(clonedItemStack, "commandReward");
    if (commandReward != null) {
      if (!commandReward.startsWith("/")) {
        commandReward = "/" + commandReward;
      }
      itemStackHelper.addLore("&3Command: " + commandReward).addLore("&7");
    }

    itemStackHelper
        .addLore("&e[Left Click to change chance]")
        .addLore("&e[Middle Click to change display name]")
        .addLore("&e[Right Click for more options]")
        .addLore(" ")
        .addLore("&f <-- &3[SHIFT + LC] &f&l| &3[SHIFT + RC] &f-->");

    return itemStackHelper.build();
  }

  public ItemStack generateDisplayRewardItemStack(CrateItem crateItem, BigDecimal chance) {
    ItemStack clonedItemStack = crateItem.getItemStack().clone();
    ItemStackHelper.Builder itemStackHelper =
        ItemStackHelper.builder(clonedItemStack)
            .addLore("&7")
            .addLore("&7Display: " + crateItem.getDisplayText())
            .addLore("&7")
            .addLore("&3" + chance + "% Chance")
            .addLore("&7");

    String commandReward = NBTAPI.getNBT(clonedItemStack, "commandReward");
    if (commandReward != null) {
      if (!commandReward.startsWith("/")) {
        commandReward = "/" + commandReward;
      }
      itemStackHelper.addLore("&3Command: " + commandReward).addLore("&7");
    }

    return itemStackHelper.build();
  }

  public ItemStack generateDisplayCrateItemStack(Crate crate) {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.CHEST)
            .name(crate.getDisplayName())
            .addLore("&7" + crate.getId())
            .addLore("&7" + crate.getDescription())
            .addLore("&7");
    List<String> crateRewardsText = CrateHandler.getInstance().getCrateRewardsText(crate, 7);
    return builder.addLore(crateRewardsText).addLore("&7").addLore("&e[EDIT]").build();
  }

  public ItemStack generatePlaceCrateItemStack(Crate crate) {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.ENDER_CHEST).name(crate.getDisplayName());
    if (!crate.getDescription().isBlank()) {
      builder.addLore("&7" + crate.getDescription());
    }
    builder
        .addLore("&7")
        .addLore("&7Place anywhere at spawn unlock &e" + crate.getAmountToWin())
        .addLore("&7of a total &e" + crate.getRewardsMap().size() + " &7possible rewards!")
        .addLore("&7")
        .addLore("&7Possible Rewards:");
    List<String> crateRewardsText = CrateHandler.getInstance().getCrateRewardsText(crate, true);
    ItemStack crateItemStack = builder.addLore(crateRewardsText).build();
    NBTAPI.addNBT(crateItemStack, "crate_id", crate.getId());
    return crateItemStack;
  }

  public Crate sortRewards(Crate crate) {
    CrateManager.SortBy sortBy = crate.getSortBy();
    CrateManager.Order order = crate.getOrder();
    List<CrateItem> displaySet = crate.getDisplaySet();
    LinkedHashMap<CrateItem, BigDecimal> chanceMap = crate.getRewardsMap().getTable();
    List<CrateItem> sortedList =
        new ArrayList<>(
            switch (sortBy) {
              case DISPLAY_NAME ->
                  displaySet.stream()
                      .sorted(Comparator.comparing(CrateItem::getPlainDisplay))
                      .toList();
              case ITEM ->
                  displaySet.stream()
                      .sorted(Comparator.comparing(e -> Utils.itemName(e.getItemStack())))
                      .toList();
              case CHANCE ->
                  chanceMap.entrySet().stream()
                      .sorted(Map.Entry.comparingByValue())
                      .map(Map.Entry::getKey)
                      .toList();
              case CUSTOM -> crate.getDisplaySet();
            });
    if (order.equals(CrateManager.Order.DESCENDING)) {
      Collections.reverse(sortedList);
    }
    crate.setDisplaySet(sortedList);
    return crate;
  }
}
