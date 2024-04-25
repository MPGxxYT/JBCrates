package me.mortaldev.jbcrates.modules.animation;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class DefaultAnimation implements IAnimation {

    private static final double RADIUS = 5;
    private static final double SPEED = 0.3;
    private static final double ADD_EVERY_TURN = Math.toRadians(2);

    @Override
    public void executeAnimation(Location origin, ItemStack itemStack) {

    }
}
