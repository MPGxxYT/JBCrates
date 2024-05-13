package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Crate {

  String id;
  String displayName;
  String description = "&7This is the default description.";
  Map<String, Double> rewardsMap = new HashMap<>();
  Map<String, String> rewardsDisplayMap = new HashMap<>();
  Integer amountToWin = 1;

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
      Map<String, String> rewardsDisplayMap,
      Integer amountToWin) {
    this.displayName = displayName;
    this.id = id;
    this.description = description;
    this.rewardsMap = rewardsMap;
    this.rewardsDisplayMap = rewardsDisplayMap;
    this.amountToWin = amountToWin;
  }

  public Crate clone() {
    return new Crate(id, displayName, description, rewardsMap, rewardsDisplayMap, amountToWin);
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
    for (Map.Entry<String, Double> entry : rewardsMap.entrySet()) {
      convertedMap.put(decodeItemStack(entry.getKey()), entry.getValue());
    }
    return convertedMap;
  }

  public Map<ItemStack, Component> getRewardsDisplayMap() {
    Map<ItemStack, Component> convertedMap = new HashMap<>();
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
    for (Map.Entry<ItemStack, Double> entry : rewardsMap.entrySet()) {
      this.rewardsMap.put(encodeItemStack(entry.getKey()), entry.getValue());
    }
  }

  public void setRewardsDisplayMap(Map<ItemStack, Component> rewardsDisplayMap) {
    this.rewardsDisplayMap.clear();
    for (Map.Entry<ItemStack, Component> entry : rewardsDisplayMap.entrySet()) {
      this.rewardsDisplayMap.put(
          encodeItemStack(entry.getKey()), TextUtil.serializeComponent(entry.getValue()));
    }
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
