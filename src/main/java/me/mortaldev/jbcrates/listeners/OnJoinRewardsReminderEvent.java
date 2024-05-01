package me.mortaldev.jbcrates.listeners;

import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoinRewardsReminderEvent implements Listener {

  @EventHandler
  public void playerJoined(PlayerJoinEvent event){
    Player player = event.getPlayer();
    if (CrateProfileManager.hasCrateProfile(player.getUniqueId())) {
      player.sendMessage(TextUtil.format("&6[REWARDS!]&f You have /getCrateRewards to claim!"));
    }
  }
}
