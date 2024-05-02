package me.mortaldev.jbcrates.modules.crate;

import com.destroystokyo.paper.ParticleBuilder;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.animation.DefaultAnimation;
import me.mortaldev.jbcrates.modules.profile.CrateProfile;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import me.mortaldev.jbcrates.records.Pair;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CrateExecutor {
  private final Map<Item, Integer> itemIntegerMap = new HashMap<>();
  private final Crate crate;
  private double height = 1;
  private int popCounter;
  private Location location;
  private List<ItemStack> winningItems;
  private Player player;
  private final Main MAIN_INSTANCE = Main.getInstance();
  private CrateProfile crateProfile;

  public interface Callback {
    void onCompletion();
  }

  public interface CallbackItem {
    void onCompletion(Item item, Boolean b, Integer taskID);
  }

  public CrateExecutor(Crate crate) {
    this.crate = crate;
  }

  public void execute(Block block, Player player) {
    this.player = player;
    winningItems = getWinningCrateItems(crate);

    crateProfile = new CrateProfile(player.getUniqueId());
    for (ItemStack item : winningItems) {
      crateProfile.addItem(item);
    }
    CrateProfileManager.addCrateProfile(crateProfile);
    CrateProfileManager.addToCrateActiveList(player.getUniqueId());

    location = block.getLocation().clone().add(0, 2, 0);
    Main.getInstance().addCrateLocation(location);
    animateCrateMovement(block, this::runMainAnimation);
  }

  void runMainAnimation() {
    final int[] i = {1};
    long startMulti = 120 / crate.getRewardsMap().size();
    Map<ItemStack, Component> rewardsDisplayMap = crate.getRewardsDisplayMap();
    for (Map.Entry<ItemStack, Component> entry : rewardsDisplayMap.entrySet()) {
      boolean winning = winningItems.contains(entry.getKey());
      Bukkit.getScheduler()
          .scheduleSyncDelayedTask(
              MAIN_INSTANCE,
              () ->
                  new DefaultAnimation()
                      .animate(
                          location.toCenterLocation(),
                          entry.getKey(),
                          140,
                          winning,
                          entry.getValue(),
                          (item, bool, taskID) -> {
                            if (bool) {
                              itemIntegerMap.put(item, taskID);
                            }
                            finishAnimation();
                          }),
              (i[0] * startMulti) - startMulti);
      i[0]++;
    }
  }

  void popItem(Item item) {
    new ParticleBuilder(Particle.SMOKE_NORMAL).location(item.getLocation()).count(5).spawn();
    item.remove();
  }

  void finishAnimation() {
    popCounter++;
    if (popCounter == crate.getRewardsDisplayMap().size()) {
      Bukkit.getScheduler()
          .scheduleSyncDelayedTask(
              MAIN_INSTANCE,
              () -> {
                for (Map.Entry<Item, Integer> entry : itemIntegerMap.entrySet()) {
                  Bukkit.getScheduler().cancelTask(entry.getValue());
                  entry.getKey().setVelocity(new Vector(0, 0, 0));
                  entry.getKey().teleport(location.clone().toCenterLocation().add(0, height, 0));
                  height += 0.75;
                }
                if (player.isOnline() && !CrateProfileManager.getWasOfflineList().contains(player.getUniqueId())) {
                  for (ItemStack itemStack : winningItems) {
                      player.sendMessage(
                          TextUtil.format(
                              "&6You have won &f").append(crate.getRewardsDisplayMap().get(itemStack)).append(TextUtil.format("&6.")));
                      if (Utils.canInventoryHold(player.getInventory(), 1)) {
                        player.getInventory().addItem(itemStack);
                        crateProfile.removeItem(itemStack);
                      } else {
                        player.sendMessage(
                            TextUtil.format(
                                "&cYour inventory was full. Reward added to /getCrateRewards."));
                      }
                      if (!crateProfile.getItemList().isEmpty()) {
                        CrateProfileManager.updateCrateProfile(crateProfile);
                      } else {
                        CrateProfileManager.removeCrateProfile(crateProfile);
                      }
                    }
                  }
                CrateProfileManager.removeFromCrateActiveList(player.getUniqueId());
                if (CrateProfileManager.getWasOfflineList().contains(player.getUniqueId())) {
                  CrateProfileManager.removeFromWasOfflineList(player.getUniqueId());
                }
                Bukkit.getScheduler()
                    .scheduleSyncDelayedTask(
                        MAIN_INSTANCE,
                        () -> {
                          for (Item item : itemIntegerMap.keySet()) {
                            popItem(item);
                          }
                          location.getBlock().setType(Material.AIR);
                          Main.getInstance().removeCrateLocation(location);
                        },
                        60);
              },
              60);
    }
  }

  void animateCrateMovement(Block block, Callback callback) {
    BlockData blockData = block.getBlockData();
    ParticleBuilder particleBuilder = new ParticleBuilder(Particle.SCRAPE);
    Location location = block.getLocation();
    for (int i = 0; i < 2; i++) {
      int finalI = i;
      Bukkit.getScheduler()
          .scheduleSyncDelayedTask(
              MAIN_INSTANCE,
              () -> {
                location.getBlock().setType(Material.AIR);
                location.add(0, 1, 0).getBlock().setBlockData(blockData);
                particleBuilder
                    .count(10)
                    .offset(-0.5, -0.5, -0.5)
                    .location(location.toCenterLocation())
                    .spawn();
                if (finalI == 1 && callback != null) {
                  animateOpenCrate(callback);
                }
              },
              10 * (i + 1));
    }
  }

  void animateOpenCrate(Callback callback) {
    Block block = location.getBlock();
    if (block.getState() instanceof Lidded liddedBlock) {
      liddedBlock.open();
      //      new ParticleBuilder(Particle.ENCHANTMENT_TABLE)
      //          .location(location.toCenterLocation().add(0, 1.5, 0))
      //          .count(10)
      //          .spawn();
    }
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            MAIN_INSTANCE,
            () -> {
              new ParticleBuilder(Particle.SONIC_BOOM)
                  .location(location.toCenterLocation().add(0, 0.5, 0))
                  .count(5)
                  .spawn();
              Bukkit.getScheduler()
                  .scheduleSyncDelayedTask(MAIN_INSTANCE, callback::onCompletion, 10);
            },
            30);
  }

  private List<ItemStack> getWinningCrateItems(Crate crate) {
    List<ItemStack> winningItems = new ArrayList<>();
    Map<ItemStack, Double> rewardsMap = crate.getRewardsMap();

    for (int i = 0; i < crate.getAmountToWin(); i++) {

      Pair<Map<ItemStack, Pair<Integer, Integer>>, Integer> mapIntegerPair =
          calculateMinMax(rewardsMap);
      Map<ItemStack, Pair<Integer, Integer>> minMap = mapIntegerPair.first();
      int max = mapIntegerPair.second();

      int generatedNumber = ThreadLocalRandom.current().nextInt(0, max);

      for (Map.Entry<ItemStack, Pair<Integer, Integer>> entry : minMap.entrySet()) {
        if (generatedNumber >= entry.getValue().first()
            && generatedNumber < entry.getValue().second()) {
          winningItems.add(entry.getKey());
          rewardsMap.remove(entry.getKey());
          break;
        }
      }
    }
    return winningItems;
  }

  private static Pair<Map<ItemStack, Pair<Integer, Integer>>, Integer> calculateMinMax(
      Map<ItemStack, Double> map) {
    Map<ItemStack, Pair<Integer, Integer>> minMap = new HashMap<>();
    int total = 0;
    for (Map.Entry<ItemStack, Double> entry : map.entrySet()) {
      int adjustedChance = (int) ((entry.getValue()) * 100);
      int min = total;
      int max = total + adjustedChance;
      total += adjustedChance;
      minMap.put(entry.getKey(), new Pair<>(min, max));
    }
    return new Pair<>(minMap, total);
  }
}
