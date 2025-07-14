package me.mortaldev.jbcrates.modules.animation;

import java.util.List;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;

public interface IAnimation {
  void animate(AnimationData animationData, Consumer<List<ItemStack>> callback);
}
