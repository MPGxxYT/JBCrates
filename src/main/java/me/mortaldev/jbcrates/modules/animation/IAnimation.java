package me.mortaldev.jbcrates.modules.animation;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface IAnimation {

    // Not sure how I want this to work yet.
    void executeAnimation(Location origin, ItemStack itemStack);

}
