package me.mortaldev.jbcrates.modules.crate;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Crate {
    String id;
    String displayName;
    String description = "&7This is the default description.";
    Map<String, Float> rewardsMap = new HashMap<>();

    public Crate(String displayName) {
        this.displayName = displayName;
        this.id = CrateManager.stringToIDFormat(displayName);
    }

    public Crate(String id, String displayName, String description, Map<String, Float> rewardsMap) {
        this.displayName = displayName;
        this.id = id;
        this.description = description;
        this.rewardsMap = rewardsMap;
    }

    public Crate clone(){
        return new Crate(id, displayName, description, rewardsMap);
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

    public Map<ItemStack, Float> getRewardsMap() {
        Map<ItemStack, Float> convertedMap = new HashMap<>();
        for (Map.Entry<String, Float> entry : rewardsMap.entrySet()) {
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

    public void setRewardsMap(Map<ItemStack, Float> rewardsMap) {
        this.rewardsMap.clear();
        for (Map.Entry<ItemStack, Float> entry : rewardsMap.entrySet()) {
            this.rewardsMap.put(encodeItemStack(entry.getKey()), entry.getValue());
        }
    }

    public void addReward(ItemStack itemStack, Float chance){
        rewardsMap.put(encodeItemStack(itemStack), chance);
    }
    public void removeReward(ItemStack itemStack){
        rewardsMap.remove(encodeItemStack(itemStack));
    }

    String encodeItemStack(ItemStack itemStack){
        byte[] itemStackAsBytes = itemStack.serializeAsBytes();
        return Base64.getEncoder().encodeToString(itemStackAsBytes);
    }

    ItemStack decodeItemStack(String string){
        byte[] bytes = Base64.getDecoder().decode(string);
        return ItemStack.deserializeBytes(bytes);
    }

}
