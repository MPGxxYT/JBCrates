package me.mortaldev.jbcrates.modules.animation;

import com.destroystokyo.paper.ParticleBuilder;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.CrateExecutor;
import me.mortaldev.jbcrates.utils.NBTAPI;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultAnimation {
  private final double RADIUS = 5;
  private final double SPEED = 0.3;
  private final double ADD_EVERY_TURN = Math.toRadians(2);

  public void animate(
      Location locationOfCrate,
      ItemStack itemStack,
      Integer popDelay,
      boolean IsWinningItem,
      String display,
      CrateExecutor.CallbackItem callback) {
    var world = locationOfCrate.getWorld();

    final double[] t = {0}; // Time variables
    double centerX = locationOfCrate.getX();
    double centerY = locationOfCrate.getY();
    double centerZ = locationOfCrate.getZ();
    AtomicReference<Double> tilt =
        new AtomicReference<>(Math.toRadians(45)); // Tilt angle (45 degrees in this case)

    ItemStack clonedItemStack = itemStack.clone();
    String newDisplay = display + " &fx" + clonedItemStack.getAmount();
    clonedItemStack.setAmount(1);
    NBTAPI.addNBT(clonedItemStack, "UUID", UUID.randomUUID().toString());

    var itemEntity = world.dropItem(locationOfCrate, clonedItemStack);

    itemEntity.setGravity(false);
    itemEntity.setVelocity(new Vector(0, 0.5, 0));
    itemEntity.setCanPlayerPickup(false);
    itemEntity.setCanMobPickup(false);

    itemEntity.customName(TextUtil.format(newDisplay));
    itemEntity.setCustomNameVisible(true);

    AtomicInteger i = new AtomicInteger();
    AtomicInteger pop = new AtomicInteger();

    Map<String, Integer> taskReturn = new HashMap<>();
    taskReturn.put(
        "movingThings",
        Bukkit.getServer()
            .getScheduler()
            .scheduleSyncRepeatingTask(
                Main.getInstance(),
                () -> {
                  double x = centerX + RADIUS * Math.cos(t[0]);
                  double z = centerZ + RADIUS * Math.sin(t[0]);

                  // Adjust y coordinate to introduce tilt
                  double y = centerY + RADIUS * Math.sin(tilt.get()) * Math.sin(t[0]);

                  Vector velocity =
                      new Vector(
                              x - itemEntity.getLocation().getX(),
                              y - itemEntity.getLocation().getY(),
                              z - itemEntity.getLocation().getZ())
                          .normalize()
                          .multiply(SPEED);

                  itemEntity.setVelocity(velocity);
                  if (i.get() == 5) {
                    new ParticleBuilder(Particle.SCRAPE).location(itemEntity.getLocation()).spawn();
                    i.set(0);
                  } else {
                    i.getAndIncrement();
                  }

                  t[0] += Math.PI / 16; // 16 is the number of positions in the circle.
                  tilt.set(tilt.get() + ADD_EVERY_TURN);
                  pop.getAndIncrement();
                  if (pop.get() == popDelay) {
                    if (!IsWinningItem) {
                      popItem(itemEntity);
                      callback.onCompletion(itemEntity, false, taskReturn.get("movingThings"));
                      Bukkit.getScheduler().cancelTask(taskReturn.remove("movingThings"));
                    } else {
                      callback.onCompletion(itemEntity, true, taskReturn.get("movingThings"));
                    }
                  }
                },
                0L,
                1L)); // Starting delay and interval (in server ticks))
  }

  void popItem(Item item) {
    new ParticleBuilder(Particle.SMOKE_NORMAL).location(item.getLocation()).count(5).spawn();
    item.remove();
  }
}
