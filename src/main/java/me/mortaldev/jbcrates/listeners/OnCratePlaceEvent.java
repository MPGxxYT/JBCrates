package me.mortaldev.jbcrates.listeners;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.mortaldev.jbcrates.configs.MainConfig;
import me.mortaldev.jbcrates.modules.animation.AnimationData;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.profile.CrateProfile;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import me.mortaldev.jbcrates.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class OnCratePlaceEvent implements Listener {
  public static final String CRATE_TAG = "crate_id";
  public static final Cooldown cooldown = new Cooldown();

  @EventHandler
  public void onClick(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    ItemStack itemInHand =
        event.getItemInHand().getType().equals(Material.ENDER_CHEST) ? event.getItemInHand() : null;
    if (itemInHand == null) {
      return;
    }
    String crateID = NBTAPI.getNBT(itemInHand, CRATE_TAG);
    Location location = event.getBlockPlaced().getLocation();
    if (crateID == null) {
      return;
    }
    Map<String, Integer> worldWhitelistMap = MainConfig.getInstance().getWorldWhitelist();
    Integer crateCooldown = MainConfig.getInstance().getCrateCooldown();
    if (!cooldown.isDone(player.getUniqueId())) {
      int timeLeft = cooldown.getTimeLeft(player.getUniqueId());
      player.sendMessage(
          TextUtil.format("&cYou can open another crate again in " + timeLeft + " seconds."));
      event.setCancelled(true);
      return;
    }
    Optional<CrateProfile> crateProfileOptional =
        CrateProfileManager.getInstance().getByID(player.getUniqueId().toString());
    if (crateProfileOptional.isPresent()) {
      CrateProfile crateProfile = crateProfileOptional.get();
      List<ItemStack> overflowRewards = crateProfile.getOverflowRewards();
      if (!overflowRewards.isEmpty()) {
        event.setCancelled(true);
        player.sendMessage(
            TextUtil.format(
                "&cYou must get your &l/getCrateRewards&c before you can open another crate."));
      }
      return;
    }
    if (!location.clone().add(0, 1, 0).getBlock().getType().equals(Material.AIR)
        || !location.clone().add(0, 2, 0).getBlock().getType().equals(Material.AIR)) {
      event.setCancelled(true);
      player.sendMessage(TextUtil.format("&cYou cannot open it here. Try somewhere else."));
      return;
    }
    boolean isWrongWorld = true;
    for (Map.Entry<String, Integer> entry : worldWhitelistMap.entrySet()) {
      if (location.getWorld().getName().equalsIgnoreCase(entry.getKey())
          && location.getY() >= entry.getValue()) {
        isWrongWorld = false;
      }
    }
    if (isWrongWorld) {
      event.setCancelled(true);
      player.sendMessage(TextUtil.format("&cYou cannot open it here. Try somewhere else."));
      return;
    }
    Optional<Crate> crateOptional = CrateManager.getInstance().getByID(crateID);
    if (crateOptional.isEmpty()) {
      Bukkit.getLogger().warning("FAILED TO GET CRATE FROM ID: " + crateID);
      event.setCancelled(true);
      return;
    }
    Crate crate = crateOptional.get();
    if (crate.getRewardsMap().isEmpty()) {
      event.setCancelled(true);
      player.sendMessage(TextUtil.format("&cThis crate has nothing in it. Cannot open."));
      return;
    }
    BlockData blockData = event.getBlock().getBlockData();
    event.setCancelled(true);
    location.getBlock().setBlockData(blockData, false);
    int amount = itemInHand.getAmount() - 1;
    EquipmentSlot hand = event.getHand();
    if (amount <= 0) {
      player.getInventory().setItem(hand, new ItemStack(Material.AIR));
    } else {
      itemInHand.setAmount(amount);
      player.getInventory().setItem(hand, itemInHand);
    }
    AnimationData animationData = new AnimationData(crate, location);
    cooldown.start(player.getUniqueId(), crateCooldown.longValue());
    crate
        .getAnimation()
        .getIAnimation()
        .animate(
            animationData,
            (winningItems) ->
                CrateProfileManager.getInstance()
                    .giveItems(winningItems.toArray(new ItemStack[0]), player));
  }
}
