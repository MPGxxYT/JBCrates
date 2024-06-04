package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.NBTAPI;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

// This whole class needs to be reorganized and updated.

public class CrateManager {
  static List<Crate> crateList;

  public static void updateCratesList() {
    crateList = new ArrayList<>();
    File mainPath = new File(CrateCRUD.getMainFilePath());
    if (!mainPath.exists()) {
      return;
    }
    for (File file : mainPath.listFiles()) {
      Crate crate = CrateCRUD.getCrate(file.getName().replace(".json", ""));
      if (crate.getDescription().contains("§")) {
        String string = crate.getDescription().replaceAll("§", "&");
        crate.setDescription(string);
      }
      crateList.add(crate);
    }
  }

  public static List<Crate> getCrates() {
    if (crateList == null) {
      updateCratesList();
    }
    return crateList;
  }

  public static String stringToIDFormat(String string) {
    string = TextUtil.removeDecoration(string);
    string = TextUtil.removeColors(string);
    string = string.replaceAll(" ", "_").toLowerCase();
    return string.replaceAll("\\W", "").toLowerCase();
  }

  public static boolean crateByIDExists(String id) {
    String newID = stringToIDFormat(id);
    for (Crate crate : getCrates()) {
      if (crate.getId().equals(newID)) {
        return true;
      }
    }
    return false;
  }

  public static int crateByIDCount(String id) {
    String newID = stringToIDFormat(id);
    int count = 0;
    for (Crate crate : getCrates()) {
      if (crate.getId().replaceAll("_\\d+", "").equals(newID)) {
        count++;
      }
    }
    return count;
  }

  public static Crate getCrate(String id) {
    for (Crate crate : getCrates()) {
      if (crate.getId().equals(id)) {
        return crate;
      }
    }
    return null;
  }

  public static void addCrate(Crate crate) {
    if (crateByIDExists(crate.getId())) {
      updateCrate(crate.getId(), crate);
      return;
    }
    crateList.add(crate);
    CrateCRUD.saveCrate(crate);
  }

  public static void removeCrate(Crate crate) {
    if (!crateByIDExists(crate.getId())) {
      return;
    }
    crateList.remove(crate);
    CrateCRUD.deleteCrate(crate.id);
  }

  public static void updateCrate(String crateID, Crate newCrate) {
    crateList.remove(getCrate(crateID));
    crateList.add(newCrate);
    CrateCRUD.saveCrate(newCrate);
  }

  public static List<Component> getCrateRewardsText(Crate crate) {
    return getCrateRewardsText(crate, crate.getRewardsMap().size(), false);
  }

  public static List<Component> getCrateRewardsText(Crate crate, boolean displayMode) {
    return getCrateRewardsText(crate, crate.getRewardsMap().size(), displayMode);
  }

  public static List<Component> getCrateRewardsText(Crate crate, int maxItemsListed) {
    return getCrateRewardsText(crate, maxItemsListed, false);
  }

  public static List<Component> getCrateRewardsText(
      Crate crate, int maxItemsListed, boolean displayMode) {
    List<Component> rewardsText = new ArrayList<>();
    if (crate.getRewardsMap().isEmpty() || crate.getRewardsMap() == null) {
      rewardsText.add(TextUtil.format("&cNo Rewards Inside."));
    } else {
      int j = 0;
      LinkedHashMap<ItemStack, Component> rewardsDisplayMap = crate.getRewardsDisplayMap();
      for (Map.Entry<ItemStack, Component> entry : rewardsDisplayMap.entrySet()) {
        if (j <= maxItemsListed) {
          j++;
          ItemStack itemStack = entry.getKey();
          Component displayName = entry.getValue();
          Double chance = crate.getRewardsMap().get(itemStack);
          if (displayMode) {
            rewardsText.add(
                TextUtil.format("&7 - ")
                    .append(displayName)
                    .append(TextUtil.format(" x" + itemStack.getAmount())));
          } else {
            rewardsText.add(
                TextUtil.format("&7")
                    .append(displayName)
                    .append(
                        TextUtil.format(
                            " x" + itemStack.getAmount() + " &3" + chance + "% Chance")));
          }
        } else {
          break;
        }
      }
      int itemsLeft = rewardsDisplayMap.size() - maxItemsListed;
      if (itemsLeft > 0) {
        rewardsText.add(TextUtil.format("&7..." + itemsLeft + " more"));
      }
    }
    return rewardsText;
  }

  public static Double getRewardChancesTotal(Crate crate) {
    BigDecimal total = new BigDecimal("0");
    for (Double value : crate.getRewardsMap().values()) {
      total = total.add(new BigDecimal(value)).setScale(2, RoundingMode.HALF_UP);
    }
    return total.doubleValue();
  }

  public static boolean rewardChancesIsBalanced(Crate crate) {
    BigDecimal total = new BigDecimal("0");
    for (Double value : crate.getRewardsMap().values()) {
      total = total.add(new BigDecimal(value)).setScale(2, RoundingMode.HALF_UP);
    }
    return total.equals(new BigDecimal("100"));
  }

  public static void balanceRewardChances(Crate crate) {
    if (rewardChancesIsBalanced(crate)) {
      return;
    }
    double sum = 0d;
    for (Double value : crate.getRewardsMap().values()) {
      sum += value;
    }
    Map<ItemStack, Double> rewardsMap = crate.getRewardsMap();
    int i = 0;
    BigDecimal total = new BigDecimal("0");
    for (Map.Entry<ItemStack, Double> entry : rewardsMap.entrySet()) {
      BigDecimal scaledValue;
      if (i == crate.getRewardsMap().values().size() - 1) {
        scaledValue = new BigDecimal("100").subtract(total);
      } else {
        scaledValue = new BigDecimal(entry.getValue() / sum * 100);
        scaledValue = scaledValue.setScale(2, RoundingMode.HALF_UP);
      }
      total = total.add(scaledValue);
      //      BigDecimal rescaledTotal = total.setScale(2, RoundingMode.HALF_UP);
      //      Bukkit.getLogger().info(scaledValue.toPlainString() + " : " +
      // rescaledTotal.toPlainString());
      entry.setValue(scaledValue.doubleValue());
      i++;
    }
    crate.setRewardsMap(rewardsMap);
  }

  public static ItemStack generateEditingRewardItemStack(
      ItemStack itemStack, Double chance, Component display) {

    ItemStack clonedItemStack = itemStack.clone();
    ItemStackHelper.Builder itemStackHelper =
        ItemStackHelper.builder(clonedItemStack)
            .addLore("&7")
            .addLore(TextUtil.format("&7Display: ").append(display))
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

  public static ItemStack generateDisplayRewardItemStack(
      ItemStack itemStack, Double chance, Component display) {

    ItemStack clonedItemStack = itemStack.clone();
    ItemStackHelper.Builder itemStackHelper =
        ItemStackHelper.builder(clonedItemStack)
            .addLore("&7")
            .addLore(TextUtil.format("&7Display: ").append(display))
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

  public static ItemStack generateDisplayCrateItemStack(Crate crate) {
    ItemStackHelper.Builder builder =
        ItemStackHelper.builder(Material.CHEST)
            .name(crate.getDisplayName())
            .addLore("&7" + crate.getId())
            .addLore("&7" + crate.getDescription())
            .addLore("&7");
    List<Component> crateRewardsText = CrateManager.getCrateRewardsText(crate, 7);
    return builder.addLore(crateRewardsText).addLore("&7").addLore("&e[EDIT]").build();
  }

  public static ItemStack generatePlaceCrateItemStack(Crate crate) {
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
    List<Component> crateRewardsText = CrateManager.getCrateRewardsText(crate, true);
    ItemStack crateItemStack = builder.addLore(crateRewardsText).build();
    NBTAPI.addNBT(crateItemStack, "crate_id", crate.getId());
    return crateItemStack;
  }

  public static LinkedHashMap<ItemStack, Component> sortRewards(
      SortBy sortBy, Order order, Crate crate) {

    if (sortBy.equals(SortBy.DISPLAY_NAME)){
      LinkedHashMap<ItemStack, Component> rewardsDisplayMap = crate.getRewardsDisplayMap();
      Comparator<Map.Entry<ItemStack, Component>> comparator =
          Comparator.comparing(e -> TextUtil.componentToString(e.getValue()));
      comparator = adjustComparatorOrder(order, comparator);

      return rewardsDisplayMap.entrySet().stream()
          .sorted(comparator)
          .collect(
              Collectors.toMap(
                  Map.Entry::getKey,
                  Map.Entry::getValue,
                  (oldValue, newValue) -> oldValue,
                  LinkedHashMap::new));
    } else if (sortBy.equals(SortBy.CUSTOM)){
        LinkedHashMap<ItemStack, Component> reversedMap = new LinkedHashMap<>();
        new LinkedList<>(crate.getRewardsDisplayMap().entrySet())
            .descendingIterator()
            .forEachRemaining(entry -> reversedMap.put(entry.getKey(), entry.getValue()));
        return reversedMap;
    } else {
      Comparator<Map.Entry<ItemStack, Double>> comparator = getComparatorForNonDisplayNameSort(sortBy);
      comparator = adjustComparatorOrder(order, comparator);
      Map<ItemStack, Double> rewardsMap = crate.getRewardsMap();

      return rewardsMap.entrySet().stream()
          .sorted(comparator)
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              entry -> crate.getRewardDisplay(entry.getKey()),
              (oldValue, newValue) -> oldValue,
              LinkedHashMap::new));
    }
  }

  private static <T> Comparator<Map.Entry<ItemStack, T>> adjustComparatorOrder(Order order, Comparator<Map.Entry<ItemStack, T>> comparator) {
    if (order.equals(Order.DESCENDING)) {
      return comparator.reversed();
    }
    return comparator;
  }

  private static Comparator<Map.Entry<ItemStack, Double>> getComparatorForNonDisplayNameSort(SortBy sortBy) {
    return switch (sortBy) {
      case CHANCE -> Map.Entry.comparingByValue();
      case ITEM -> Comparator.comparing(e -> Utils.itemName(e.getKey()));
      default -> (entry1, entry2) -> 0;
    };
  }

  public enum Order {
    ASCENDING("Ascending"),
    DESCENDING("Descending");

    private final String name;

    Order(String name) {
      this.name = name;
    }

    public static Order flip(Order order){
      switch (order) {
        case ASCENDING -> {
          return DESCENDING;
        }
        case DESCENDING -> {
          return ASCENDING;
        }
      }
      return ASCENDING;
    }

    public String getName() {
      return name;
    }
  }

  public enum SortBy {
    CHANCE("Chance"),
    DISPLAY_NAME("Display Name"),
    ITEM("Item Name"),
    CUSTOM("Custom");

    private final String name;

    SortBy(String name) {
      this.name = name;
    }

    public static SortBy next(SortBy sortBy){
      for (int i = 0; i < values().length; i++) {
        SortBy value = values()[i];
        if (value == sortBy) {
          if (i+1 >= values().length) {
            return values()[0];
          } else {
            return values()[i+1];
          }
        }
      }
      return values()[0];
    }

    public String getName() {
      return name;
    }
  }
}
