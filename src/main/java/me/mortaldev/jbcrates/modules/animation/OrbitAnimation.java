package me.mortaldev.jbcrates.modules.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateItem;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class OrbitAnimation implements IAnimation {

  // --- Animation constants to easily tweak the feel of the animation ---

  // Chest Rise Phase
  private static final double CHEST_RISE_HEIGHT = 3.0;
  private static final long CHEST_RISE_DURATION_TICKS = 40L; // 2 seconds
  private static final int RISING_PARTICLE_COUNT = 2; // Number of swirling particles

  // Orbit Phase
  private static final long ORBIT_DURATION_TICKS = 200L; // 6 seconds
  private static final double ORBIT_SPEED = 0.05;
  private static final float ITEM_SCALE = 0.5f;

  // Dynamic radius settings
  private static final double MIN_ORBIT_RADIUS = 2.0;
  private static final double MAX_ORBIT_RADIUS = 4.0;
  private static final double ORBIT_ITEM_SPACING = 1.2; // Desired space between items in the circle

  // Item spawn timing settings
  private static final long ITEM_TOTAL_SPAWN_DURATION_TICKS =
      80L; // Time over which all items appear
  private static final long ITEM_SPAWN_TRANSITION_TICKS =
      30L; // Time for an item to fly from chest to orbit

  // Winners Phase constants for sequential stacking
  private static final long WINNER_ITEM_RISE_DURATION_TICKS = 10L; // Time for one winner to rise
  private static final long WINNERS_STACK_DELAY_TICKS = 5L; // Delay between each winner rising
  private static final double WINNERS_VERTICAL_SPACING = 1.0;

  @Override
  public void animate(AnimationData animationData, Consumer<List<ItemStack>> callback) {
    // Each animation is a new instance to keep it self-contained
    new AnimationInstance(animationData, callback).start();
  }

  /** An inner class to manage the state and entities for a single animation instance. */
  private static class AnimationInstance {
    private final Consumer<List<ItemStack>> callback;
    private final Location center;
    private final Crate crate;
    private final List<ItemStack> winningItems;

    private BlockDisplay chestDisplay;
    private final List<ItemDisplay> allItemDisplays = new ArrayList<>();
    private final List<ItemDisplay> winningItemDisplays = new ArrayList<>();
    private final Map<ItemDisplay, Long> itemSpawnTicks = new HashMap<>();

    public AnimationInstance(AnimationData data, Consumer<List<ItemStack>> callback) {
      this.callback = callback;
      this.crate = data.crate();
      // Center the location for smoother rotation
      this.center = data.location().clone().add(0.5, 0, 0.5);
      // Determine the winners right away
      this.winningItems =
          crate.getRewardsMap().roll(crate.getAmountToWin(), false).stream()
              .map(CrateItem::getItemStack)
              .collect(Collectors.toList());
    }

    /** Starts the first phase of the animation. */
    public void start() {
      spawnChest();
      riseChest();
    }

    /** Phase 1: Spawns the Ender Chest display entity. */
    private void spawnChest() {
      center.getWorld().playSound(center, Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 0.5f);
      chestDisplay = (BlockDisplay) center.getWorld().spawnEntity(center, EntityType.BLOCK_DISPLAY);
      chestDisplay.setBlock(Material.ENDER_CHEST.createBlockData());

      // Offset the display entity so it's centered on the block's location
      Transformation transformation = chestDisplay.getTransformation();
      transformation.getTranslation().set(new Vector3f(-0.5f, 0, -0.5f));
      chestDisplay.setTransformation(transformation);
    }

    /** Phase 1.5: Makes the chest slowly rise up with a particle effect. */
    private void riseChest() {
      new BukkitRunnable() {
        long ticks = 0;

        @Override
        public void run() {
          if (ticks >= CHEST_RISE_DURATION_TICKS) {
            this.cancel();
            spawnAndOrbitItems();
            return;
          }
          ticks++;
          double progress = (double) ticks / CHEST_RISE_DURATION_TICKS;
          double currentY = easeOutCubic(progress) * CHEST_RISE_HEIGHT;

          Transformation transformation = chestDisplay.getTransformation();
          transformation.getTranslation().set(-0.5f, (float) currentY, -0.5f);
          chestDisplay.setTransformation(transformation);

          // Swirling particle effect
          Location particleCenter = center.clone().add(0, currentY, 0);
          for (int i = 0; i < RISING_PARTICLE_COUNT; i++) {
            double angle = (ticks * 0.15) + (i * Math.PI); // Offset particles
            double xOffset = Math.cos(angle) * 1.2;
            double zOffset = Math.sin(angle) * 1.2;
            Location particleLoc = particleCenter.clone().add(xOffset, 0.5, zOffset);
            particleCenter.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 0, 0, 0, 0, 0);
          }
        }
      }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    /** Phase 2 & 3: Spawns items that fly out to join a dynamically-sized orbit. */
    private void spawnAndOrbitItems() {
      Location chestTop = center.clone().add(0, CHEST_RISE_HEIGHT + 1, 0);
      chestTop.getWorld().playSound(chestTop, Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
      chestTop.getWorld().spawnParticle(Particle.PORTAL, chestTop, 50, 0.5, 0.5, 0.5, 0.1);

      final List<CrateItem> allPossibleItems = new ArrayList<>(crate.getDisplaySet());
      if (allPossibleItems.isEmpty()) {
        new BukkitRunnable() {
          @Override
          public void run() {
            finishAnimation();
          }
        }.runTaskLater(Main.getInstance(), 40L);
        return;
      }

      // Dynamic orbit radius calculation
      final int itemCount = allPossibleItems.size();
      double calculatedRadius = (itemCount * ORBIT_ITEM_SPACING) / (2 * Math.PI);
      final double dynamicOrbitRadius =
          Math.max(MIN_ORBIT_RADIUS, Math.min(MAX_ORBIT_RADIUS, calculatedRadius));

      final Location orbitCenter = center.clone().add(0, CHEST_RISE_HEIGHT + 1.5, 0);
      final double angleIncrement = (2 * Math.PI) / itemCount;

      // Use new constant for spawn duration, ensuring it's not longer than the orbit
      final long spawnDuration = Math.min(ITEM_TOTAL_SPAWN_DURATION_TICKS, ORBIT_DURATION_TICKS);
      final long spawnInterval = Math.max(1, spawnDuration / itemCount);

      new BukkitRunnable() {
        long ticks = 0;
        int itemsSpawned = 0;
        double currentAngle = 0;

        @Override
        public void run() {
          if (ticks >= ORBIT_DURATION_TICKS) {
            this.cancel();
            despawnLosingItems();
            return;
          }

          // --- Sequential Spawning Logic ---
          if (ticks % spawnInterval == 0 && itemsSpawned < allPossibleItems.size()) {
            CrateItem crateItem = allPossibleItems.get(itemsSpawned);
            ItemStack itemStack = crateItem.getItemStack();
            Location spawnLoc = center.clone().add(0, CHEST_RISE_HEIGHT + 1.2, 0);
            ItemDisplay itemDisplay =
                (ItemDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);

            itemDisplay.setItemStack(itemStack);
            itemDisplay.setBillboard(Display.Billboard.CENTER);
            Transformation transformation = itemDisplay.getTransformation();
            transformation.getScale().set(new Vector3f(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE));
            itemDisplay.setTransformation(transformation);

            itemDisplay.customName(
                TextUtil.format(crateItem.getDisplayText() + "&7 x" + itemStack.getAmount()));
            itemDisplay.setCustomNameVisible(true);

            itemDisplay
                .getWorld()
                .playSound(itemDisplay.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);

            allItemDisplays.add(itemDisplay);
            itemSpawnTicks.put(itemDisplay, ticks); // Store spawn tick for transition
            if (isWinningItem(itemStack)) {
              winningItemDisplays.add(itemDisplay);
            }
            itemsSpawned++;
          }

          // --- Orbiting Logic (for all currently spawned items) ---
          currentAngle += ORBIT_SPEED;
          final Location spawnPoint = center.clone().add(0, CHEST_RISE_HEIGHT + 1.2, 0);

          for (int i = 0; i < allItemDisplays.size(); i++) {
            ItemDisplay display = allItemDisplays.get(i);

            // Calculate the target orbit position for this tick
            double angle = currentAngle + (angleIncrement * i);
            double x = orbitCenter.getX() + dynamicOrbitRadius * Math.cos(angle);
            double y = orbitCenter.getY() + Math.sin(ticks * 0.1 + i) * 0.25; // Bobbing
            double z = orbitCenter.getZ() + dynamicOrbitRadius * Math.sin(angle);
            Location orbitPoint = new Location(orbitCenter.getWorld(), x, y, z);

            // Transition from spawn point to orbit
            long spawnTick = itemSpawnTicks.getOrDefault(display, 0L);
            long ticksSinceSpawn = ticks - spawnTick;

            if (ticksSinceSpawn < ITEM_SPAWN_TRANSITION_TICKS) {
              double progress =
                  easeInOutSine((double) ticksSinceSpawn / ITEM_SPAWN_TRANSITION_TICKS);
              Location lerpedLoc = lerpLocation(spawnPoint, orbitPoint, progress);
              display.teleport(lerpedLoc);
            } else {
              display.teleport(orbitPoint); // Already in orbit, just update position
            }
          }
          ticks++;
        }
      }.runTaskTimer(Main.getInstance(), 10L, 1L);
    }

    /** Phase 4: Despawns all the losing items with a "pop" effect in a random order. */
    private void despawnLosingItems() {
      List<ItemDisplay> losingDisplays = new ArrayList<>(allItemDisplays);
      losingDisplays.removeAll(winningItemDisplays);

      // ADDED: Shuffle the list for a random "pop" order
      Collections.shuffle(losingDisplays);

      long delay = 0L;
      for (ItemDisplay display : losingDisplays) {
        new BukkitRunnable() {
          @Override
          public void run() {
            display.getWorld().playSound(display.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1.5f);
            display
                .getWorld()
                .spawnParticle(Particle.ASH, display.getLocation(), 10, 0.1, 0.1, 0.1, 0.05);
            display.remove();
          }
        }.runTaskLater(Main.getInstance(), delay);
        delay += 2L; // Stagger the removal for a nice effect
      }

      // Wait for all losers to be removed before starting the final phase
      new BukkitRunnable() {
        @Override
        public void run() {
          winnersFloatUp();
        }
      }.runTaskLater(Main.getInstance(), delay + 10L);
    }

    /** Phase 5: Moves the winning items into a neat line above the chest, one by one. */
    private void winnersFloatUp() {
      final Location finalItemStackBase = center.clone().add(0, CHEST_RISE_HEIGHT + 2.0, 0);

      // Handle case with no winners to prevent getting stuck
      if (winningItemDisplays.isEmpty()) {
        finishAnimation();
        return;
      }

      for (int i = 0; i < winningItemDisplays.size(); i++) {
        final ItemDisplay display = winningItemDisplays.get(i);
        final Location startLoc = display.getLocation();
        final Location targetLoc =
            finalItemStackBase.clone().add(0, i * WINNERS_VERTICAL_SPACING, 0);
        final long startDelay = i * WINNERS_STACK_DELAY_TICKS;

        // Play a sound for each winner as it starts to rise
        new BukkitRunnable() {
          @Override
          public void run() {
            display
                .getWorld()
                .playSound(
                    display.getLocation(),
                    Sound.ENTITY_PLAYER_LEVELUP,
                    0.5f,
                    1.5f + (winningItemDisplays.indexOf(display) * 0.1f));
          }
        }.runTaskLater(Main.getInstance(), startDelay);

        // Animate the item rising
        new BukkitRunnable() {
          long ticks = 0;

          @Override
          public void run() {
            if (ticks >= WINNER_ITEM_RISE_DURATION_TICKS) {
              this.cancel();
              // If this is the last item to finish its animation, trigger the final cleanup
              if (winningItemDisplays.indexOf(display) == winningItemDisplays.size() - 1) {
                finishAnimation();
              }
              return;
            }
            ticks++;
            double progress = easeOutCubic((double) ticks / WINNER_ITEM_RISE_DURATION_TICKS);
            display.teleport(lerpLocation(startLoc, targetLoc, progress));
          }
        }.runTaskTimer(Main.getInstance(), startDelay, 1L);
      }
    }

    /** Final Phase: Cleans up all entities and executes the callback. */
    private void finishAnimation() {
      new BukkitRunnable() {
        @Override
        public void run() {
          cleanup();
          callback.accept(winningItems);
        }
      }.runTaskLater(Main.getInstance(), 60L); // 3-second delay to admire the winnings
    }

    private void cleanup() {
      if (chestDisplay != null && !chestDisplay.isDead()) {
        chestDisplay.remove();
      }
      for (ItemDisplay display : allItemDisplays) {
        if (display != null && !display.isDead()) {
          display.remove();
        }
      }
      center.getBlock().setType(Material.AIR);
      center.getWorld().playSound(center, Sound.BLOCK_ENDER_CHEST_CLOSE, 1f, 1f);
    }

    // --- Helper Methods ---

    private boolean isWinningItem(ItemStack toCheck) {
      return winningItems.stream().anyMatch(winner -> winner.equals(toCheck));
    }

    /** Linear interpolation between two values. */
    private double lerp(double start, double end, double progress) {
      return start + (end - start) * progress;
    }

    /** Linear interpolation between two locations. */
    private Location lerpLocation(Location start, Location end, double progress) {
      double x = lerp(start.getX(), end.getX(), progress);
      double y = lerp(start.getY(), end.getY(), progress);
      double z = lerp(start.getZ(), end.getZ(), progress);
      return new Location(start.getWorld(), x, y, z);
    }

    /** An easing function that starts fast and decelerates. */
    private double easeOutCubic(double t) {
      return 1 - Math.pow(1 - t, 3);
    }

    /** An easing function that starts and ends slow, providing a gentle motion. */
    private double easeInOutSine(double t) {
      return -(Math.cos(Math.PI * t) - 1) / 2;
    }
  }
}
