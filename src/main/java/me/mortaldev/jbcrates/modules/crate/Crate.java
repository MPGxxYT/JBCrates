package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Crate {
    String id;
    String displayName;
    String description = "This is the default description.";
    Map<ItemStack, Float> rewardsMap = new HashMap<>();
    //IAnimation crateAnimation = new DefaultAnimation();

    public Crate(String displayName) {
        this.displayName = displayName;
        displayName = TextUtil.removeDecoration(displayName);
        displayName = TextUtil.removeColors(displayName);
        this.id = displayName.replaceAll(" ", "_").toLowerCase();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return displayName;
    }
    public String getDescription() {
        return description;
    }

    public Map<ItemStack, Float> getRewardsMap() {
        return rewardsMap;
    }

//    public IAnimation getCrateAnimation() {
//        return crateAnimation;
//    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.displayName = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public void setRewardsMap(Map<ItemStack, Float> rewardsMap) {
        this.rewardsMap = rewardsMap;
    }

//    public void setCrateAnimation(IAnimation crateAnimation) {
//        this.crateAnimation = crateAnimation;
//    }
}
