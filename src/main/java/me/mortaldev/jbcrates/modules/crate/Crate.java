package me.mortaldev.jbcrates.modules;

import me.mortaldev.jbcrates.modules.animation.DefaultAnimation;
import me.mortaldev.jbcrates.modules.animation.IAnimation;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Crate {
    String id;
    String name;
    Map<ItemStack, Float> rewardsMap = new HashMap<>();
    IAnimation crateAnimation = new DefaultAnimation();

    public Crate(String id, String name) {
        this.name = name;
        this.id = name.replaceAll(" ", "_").toLowerCase();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<ItemStack, Float> getRewardsMap() {
        return rewardsMap;
    }

    public IAnimation getCrateAnimation() {
        return crateAnimation;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRewardsMap(Map<ItemStack, Float> rewardsMap) {
        this.rewardsMap = rewardsMap;
    }

    public void setCrateAnimation(IAnimation crateAnimation) {
        this.crateAnimation = crateAnimation;
    }
}
