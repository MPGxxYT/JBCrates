package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.jbcrates.utils.ItemStackBuilder;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
    return string.replaceAll(" ", "_").toLowerCase();
  }

  public static boolean crateByIDExists(String id) {
    String newID = stringToIDFormat(id);
    for (Crate crate : crateList) {
      if (crate.getId().equals(newID)) {
        return true;
      }
    }
    return false;
  }

  public static int crateByIDCount(String id) {
    String newID = stringToIDFormat(id);
    int count = 0;
    for (Crate crate : crateList) {
      if (crate.getId().replaceAll("_\\d+", "").equals(newID)) {
        count++;
      }
    }
    return count;
  }

  public static Crate getCrate(String id) {
    for (Crate crate : crateList) {
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
    crateList.remove(crate);
    CrateCRUD.deleteCrate(crate.id);
  }

  public static void updateCrate(String crateID, Crate newCrate) {
    crateList.remove(getCrate(crateID));
    crateList.add(newCrate);
    CrateCRUD.saveCrate(newCrate);
  }

  public static List<Component> getCrateRewardsText(Crate crate, int maxItemsListed) {
    List<Component> rewardsText = new ArrayList<>();
    if (crate.getRewardsMap().isEmpty() || crate.getRewardsMap() == null) {
      rewardsText.add(TextUtil.format("&cNo Rewards Inside."));
    } else {
      int i = 0;
      for (Map.Entry<ItemStack, Double> entry : crate.getRewardsMap().entrySet()) {
        if (i <= maxItemsListed) {
          i++;
          ItemStack itemStack = entry.getKey();
          Double chance = entry.getValue();
          String displayName = "&f" + Utils.itemName(itemStack);
          if (itemStack.getItemMeta().hasDisplayName()) {
            if (itemStack.displayName() instanceof TextComponent component) {
              displayName = component.content();
            }
          }
          rewardsText.add(
              TextUtil.format(
                  "&7" + displayName + " x" + itemStack.getAmount() + " &3" + chance + "% Chance"));
        } else {
          break;
        }
      }
    }
    return rewardsText;
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

  //

  public static ItemStack generateRewardItemStack(ItemStack itemStack, Double chance) {
    ItemStackBuilder.builder(itemStack)
        .addLore("&7")
        .addLore("&3" + chance + "% Chance")
        .addLore("&7")
        .addLore("&e[Left Click to change chance]")
        .addLore("&e[Right Click to remove reward]")
        .build();
    return itemStack;
  }

  public static ItemStack generateCrateItemStack(Crate crate) {
    ItemStackBuilder builder =
        ItemStackBuilder.builder(Material.CHEST)
            .name(crate.getDisplayName())
            .addLore("&7" + crate.getId())
            .addLore("&7" + crate.getDescription())
            .addLore("&7");
    List<Component> crateRewardsText = CrateManager.getCrateRewardsText(crate, 7);
    return builder.addLore(crateRewardsText).addLore("&7").addLore("&e[EDIT]").build();
  }
}
