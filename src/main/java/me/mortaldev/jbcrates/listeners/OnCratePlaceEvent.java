package me.mortaldev.jbcrates.listeners;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateExecutor;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
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

import java.util.Map;

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
    Crate crate;
    Location location = event.getBlockPlaced().getLocation();
    if (crateID == null) {
      return;
    }
    Map<String, Double> worldWhitelistMap = Main.getMainConfig().getWorldWhitelistMap();
    Long crateCooldown = Main.getMainConfig().getCrateCooldown();
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
      player.sendMessage(TextUtil.format("&cYou cannot open it here. Try somewhere else."));
      return;
    }
    boolean isWrongWorld = true;
    for (Map.Entry<String, Double> entry : worldWhitelistMap.entrySet()) {
      if (location.getWorld().getName().equalsIgnoreCase(entry.getKey()) && location.getY() >= entry.getValue()) {
        isWrongWorld = false;
      }
    }
    if (isWrongWorld) {
      event.setCancelled(true);
      player.sendMessage(TextUtil.format("&cYou cannot open it here. Try somewhere else."));
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
    new CrateExecutor(crate).execute(event.getBlock(), player);
    cooldown.start(player.getUniqueId(), crateCooldown);
  }
}
