package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Crate {

  String id;
  String displayName;
  String description = "&7This is the default description.";
  Map<String, Double> rewardsMap = new HashMap<>();
  LinkedHashMap<String, String> rewardsDisplayMap = new LinkedHashMap<>();
  Integer amountToWin = 1;
  CrateManager.SortBy sortBy = CrateManager.SortBy.CHANCE;
  CrateManager.Order order = CrateManager.Order.ASCENDING;

  public Crate(String displayName) {
    this(TextUtil.format(displayName));
  }

  public Crate(Component displayName) {
    this.displayName = TextUtil.serializeComponent(displayName);

    String formattedName = CrateManager.stringToIDFormat(TextUtil.componentToString(displayName));
    this.id = formattedName;

    // Adds numbers to the ID if its taken already.
    if (CrateManager.crateByIDExists(this.id)) {
      int idCount = CrateManager.crateByIDCount(this.id);
      this.id = formattedName + "_" + idCount;
      while (CrateManager.crateByIDExists(this.id)) {
        idCount++;
        this.id = formattedName + "_" + idCount;
      }
    }
  }

  private Crate(
      String id,
      String displayName,
      String description,
      Map<String, Double> rewardsMap,
      LinkedHashMap<String, String> rewardsDisplayMap,
      Integer amountToWin, CrateManager.SortBy sortBy, CrateManager.Order order) {
    this.displayName = displayName;
    this.id = id;
    this.description = description;
    this.rewardsMap = rewardsMap;
    this.rewardsDisplayMap = rewardsDisplayMap;
    this.amountToWin = amountToWin;
    this.sortBy = sortBy;
    this.order = order;
  }

  public Crate clone() {
    return new Crate(id, displayName, description, rewardsMap, rewardsDisplayMap, amountToWin, sortBy, order);
  }

  public CrateManager.SortBy getSortBy() {
    if (sortBy == null) {
      return CrateManager.SortBy.CHANCE;
    }
    return sortBy;
  }

  public void setSortBy(CrateManager.SortBy sortBy) {
    this.sortBy = sortBy;
  }

  public CrateManager.Order getOrder() {
    if (order == null) {
      return CrateManager.Order.ASCENDING;
    }
    return order;
  }

  public void setOrder(CrateManager.Order order) {
    this.order = order;
  }

  public String getId() {
    return id;
  }

  public Component getDisplayName() {
    return TextUtil.deserializeComponent(displayName);
  }

  public String getDescription() {
    return description;
  }

  public Integer getAmountToWin() {
    return amountToWin;
  }

  public Map<ItemStack, Double> getRewardsMap() {
    Map<ItemStack, Double> convertedMap = new HashMap<>();
    rewardsMap.forEach((key, value) -> convertedMap.put(decodeItemStack(key), value));
    return convertedMap;
  }

  public LinkedHashMap<ItemStack, Component> getRewardsDisplayMap() {
    LinkedHashMap<ItemStack, Component> convertedMap = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : rewardsDisplayMap.entrySet()) {
      Component display;
      ItemStack itemStack = decodeItemStack(entry.getKey());
      if (entry.getValue() == null) {
        display = fixRewardDisplay(itemStack);
      } else {
        display = TextUtil.deserializeComponent(entry.getValue());
      }
      convertedMap.put(itemStack, display);
    }
    return convertedMap;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDisplayName(String name) {
    this.displayName = TextUtil.serializeComponent(name);
  }

  public void setDisplayName(Component name) {
    this.displayName = TextUtil.serializeComponent(name);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setRewardsMap(Map<ItemStack, Double> rewardsMap) {
    this.rewardsMap.clear();
    rewardsMap.forEach((key, value) -> this.rewardsMap.put(encodeItemStack(key), value));
  }

  public void setRewardsDisplayMap(LinkedHashMap<ItemStack, Component> rewardsDisplayMap) {
    this.rewardsDisplayMap.clear();
    rewardsDisplayMap.forEach((key, value) ->
        this.rewardsDisplayMap.put(
            encodeItemStack(key), TextUtil.serializeComponent(value)));
  }

  public void setAmountToWin(Integer amountToWin) {
    this.amountToWin = amountToWin;
  }

  String encodeItemStack(ItemStack itemStack) {
    byte[] itemStackAsBytes = itemStack.serializeAsBytes();
    return Base64.getEncoder().encodeToString(itemStackAsBytes);
  }

  ItemStack decodeItemStack(String string) {
    byte[] bytes = Base64.getDecoder().decode(string);
    return ItemStack.deserializeBytes(bytes);
  }

  public void addReward(ItemStack reward, Double probability) {
    Component displayName = TextUtil.format("&f" + Utils.itemName(reward.clone()));
    if (reward.getItemMeta().hasDisplayName()) {
      displayName = reward.getItemMeta().displayName();
    }

    addReward(reward, probability, displayName);
  }

  public Component getRewardDisplay(ItemStack itemStack) {
    String encodedReward = encodeItemStack(itemStack);
    String rewardDisplay = rewardsDisplayMap.get(encodedReward);
    if (rewardDisplay == null) {
      return fixRewardDisplay(itemStack);
    }
    return TextUtil.deserializeComponent(rewardDisplay);
  }

  public Double getRewardChance(ItemStack itemStack) {
    String encodedReward = encodeItemStack(itemStack);
    return rewardsMap.get(encodedReward);
  }

  public Map.Entry<ItemStack, Double> getRewardEntry(ItemStack itemStack) {
    String encodedReward = encodeItemStack(itemStack);
    Double v = rewardsMap.get(encodedReward);
    return Map.entry(itemStack, v);
  }

  private Component fixRewardDisplay(ItemStack itemStack){
    if (itemStack.getItemMeta().hasDisplayName()) {
      Component component = itemStack.getItemMeta().displayName();
      updateReward(itemStack, component);
      return component;
    }
    Component component = TextUtil.format("&f" + Utils.itemName(itemStack.clone()));
    updateReward(itemStack, component);
    return component;
  }

  public void addReward(ItemStack reward, Double probability, Component display) {
    String serializedDisplay = GsonComponentSerializer.gson().serialize(display);
    String encodedItemStack = encodeItemStack(reward);
    this.rewardsMap.put(encodedItemStack, probability);
    this.rewardsDisplayMap.put(encodedItemStack, serializedDisplay);
  }

  public void removeReward(ItemStack reward) {
    String encodedItemStack = encodeItemStack(reward);
    if (this.rewardsMap.containsKey(encodedItemStack)) {
      this.rewardsMap.remove(encodedItemStack);
      this.rewardsDisplayMap.remove(encodedItemStack);
    }
  }

  public void updateReward(ItemStack reward, ItemStack replacement) {
    String encodedReward = encodeItemStack(reward);
    String encodedReplacement = encodeItemStack(replacement);

    Double v = this.rewardsMap.get(encodedReward);
    String string = this.rewardsDisplayMap.get(encodedReward);

    this.rewardsMap.remove(encodedReward);
    this.rewardsDisplayMap.remove(encodedReward);

    this.rewardsMap.put(encodedReplacement, v);
    this.rewardsDisplayMap.put(encodedReplacement, string);
  }

  public void updateReward(ItemStack reward, Double newProbability) {
    Component displayName = TextUtil.format("&f" + Utils.itemName(reward.clone()));
    if (reward.getItemMeta().hasDisplayName()) {
      displayName = reward.getItemMeta().displayName();
    }
    updateReward(reward, newProbability, displayName);
  }

  public void updateReward(ItemStack reward, Double newProbability, Component display) {
    String encodedItemStack = encodeItemStack(reward);
    if (this.rewardsMap.containsKey(encodedItemStack)) {
      this.rewardsMap.put(encodedItemStack, newProbability);
      this.rewardsDisplayMap.put(encodedItemStack, TextUtil.serializeComponent(display));
    }
  }

  public void updateReward(ItemStack reward, Component display) {
    String encodedItemStack = encodeItemStack(reward);
    updateReward(reward, rewardsMap.get(encodedItemStack), display);
  }
}
