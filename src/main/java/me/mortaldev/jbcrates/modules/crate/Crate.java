package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.TextComponent;
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
    Integer amountToWin = 4;

    public Crate(String displayName) {
        this.displayName = displayName;

        String formattedName = CrateManager.stringToIDFormat(displayName);
        this.id = formattedName;

        // Adds numbers to the ID based on if its taken already.
        if (CrateManager.crateByIDExists(this.id)) {
            int idCount = CrateManager.crateByIDCount(this.id);
            this.id = formattedName + "_" + idCount;
            while (CrateManager.crateByIDExists(this.id)) {
                idCount++;
                this.id = formattedName + "_" + idCount;
            }
        }
    }

    private Crate(String id, String displayName, String description, Map<String, Double> rewardsMap, Map<String, String> rewardsDisplayMap, Integer amountToWin) {
        this.displayName = displayName;
        this.id = id;
        this.description = description;
        this.rewardsMap = rewardsMap;
        this.rewardsDisplayMap = rewardsDisplayMap;
        this.amountToWin = amountToWin;
    }

    public Crate clone(){
        return new Crate(id, displayName, description, rewardsMap, rewardsDisplayMap, amountToWin);
    }

    public String getId() {
        return id;
    }
    public String getDisplayName() {
        return displayName;
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
    public Map<ItemStack, String> getRewardsDisplayMap() {
        Map<ItemStack, String> convertedMap = new HashMap<>();
        for (Map.Entry<String, String> entry : rewardsDisplayMap.entrySet()) {
            convertedMap.put(decodeItemStack(entry.getKey()), entry.getValue());
        }
        return convertedMap;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setDisplayName(String name) {
        this.displayName = name;
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
    public void setRewardsDisplayMap(Map<ItemStack, String> rewardsDisplayMap) {
        this.rewardsDisplayMap.clear();
        for (Map.Entry<ItemStack, String> entry : rewardsDisplayMap.entrySet()) {
            this.rewardsDisplayMap.put(encodeItemStack(entry.getKey()), entry.getValue());
        }
    }
    public void setAmountToWin(Integer amountToWin) {
        this.amountToWin = amountToWin;
    }

    String encodeItemStack(ItemStack itemStack){
        byte[] itemStackAsBytes = itemStack.serializeAsBytes();
        return Base64.getEncoder().encodeToString(itemStackAsBytes);
    }

    ItemStack decodeItemStack(String string){
        byte[] bytes = Base64.getDecoder().decode(string);
        return ItemStack.deserializeBytes(bytes);
    }

    public void addReward(ItemStack reward, Double probability) {
        String displayName = "&f" + Utils.itemName(reward);
        if (reward.getItemMeta().hasDisplayName()) {
            if (reward.displayName() instanceof TextComponent component) {
                displayName = component.content();
            }
        }
        addReward(reward, probability, displayName);
    }

    public void addReward(ItemStack reward, Double probability, String display) {
        String encodedItemStack = encodeItemStack(reward);
        this.rewardsMap.put(encodedItemStack, probability);
        this.rewardsDisplayMap.put(encodedItemStack, display);
    }

    public void removeReward(ItemStack reward) {
        String encodedItemStack = encodeItemStack(reward);
        if(this.rewardsMap.containsKey(encodedItemStack)){
            this.rewardsMap.remove(encodedItemStack);
            this.rewardsDisplayMap.remove(encodedItemStack);
        }
    }

    public void updateReward(ItemStack reward, Double newProbability) {
        String encodedItemStack = encodeItemStack(reward);
        if(this.rewardsMap.containsKey(encodedItemStack)){
            this.rewardsMap.put(encodedItemStack, newProbability);
        }
    }

    public void updateReward(ItemStack reward, Double newProbability, String display) {
        String encodedItemStack = encodeItemStack(reward);
        if(this.rewardsMap.containsKey(encodedItemStack)){
            this.rewardsMap.put(encodedItemStack, newProbability);
            this.rewardsDisplayMap.put(encodedItemStack, display);
        }
    }
    public void updateReward(ItemStack reward, String displayName) {
        String encodedItemStack = encodeItemStack(reward);
        if(this.rewardsMap.containsKey(encodedItemStack)){
            this.rewardsDisplayMap.put(encodedItemStack, displayName);
        }
    }

}
