package me.mortaldev.jbcrates.listeners;

import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateExecutor;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import me.mortaldev.jbcrates.utils.Cooldown;
import me.mortaldev.jbcrates.utils.NBTAPI;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class OnCratePlaceEvent implements Listener {

  private static final int Y_LEVEL = 125;
  private static final String WORLD_NAME = "spawn";
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
    Crate crate;
    Location location = event.getBlockPlaced().getLocation();
    if (crateID == null) {
      return;
    }
    // CONFIRMED A CRATE
    if (!cooldown.isDone(player.getUniqueId())) {
      int timeLeft = cooldown.getTimeLeft(player.getUniqueId());
      player.sendMessage(TextUtil.format("&cYou can open another crate again in " + timeLeft + " seconds."));
      event.setCancelled(true);
      return;
    }
    if (CrateProfileManager.hasCrateProfile(player.getUniqueId())) {
      event.setCancelled(true);
      player.sendMessage(TextUtil.format("&cYou must get your &l/getCrateRewards&c before you can open another crate."));
      return;
    }
    if (!location.clone().add(0, 1, 0).getBlock().getType().equals(Material.AIR) || !location.clone().add(0, 2, 0).getBlock().getType().equals(Material.AIR)) {
      event.setCancelled(true);
      player.sendMessage(TextUtil.format("&cYou cannot place it here. Try somewhere else."));
      return;
    }
    if (location.y() < Y_LEVEL && !location.getWorld().getName().equalsIgnoreCase(WORLD_NAME)) {
      event.setCancelled(true);
      player.sendMessage(TextUtil.format("&cYou must be at spawn to open this."));
      return;
    }
    crate = CrateManager.getCrate(crateID);
    if (crate == null) {
      Bukkit.getLogger().warning("FAILED TO GET CRATE FROM ID: " + crateID);
      event.setCancelled(true);
      return;
    }
    if (!Utils.canInventoryHold(player.getInventory(), crate.getAmountToWin())) {
      event.setCancelled(true);
      player.sendMessage(TextUtil.format("&cYou have no inventory space for this."));
      return;
    }
    new CrateExecutor(crate).execute(event.getBlock(), player);
    cooldown.start(player.getUniqueId(), 20 * 1000L);
  }
}
