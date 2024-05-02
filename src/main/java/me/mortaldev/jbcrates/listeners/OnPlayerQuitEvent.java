package me.mortaldev.jbcrates.listeners;

import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerQuitEvent implements Listener {

  @EventHandler
  public void playerQuit(PlayerQuitEvent event){
    Player player = event.getPlayer();
    if (CrateProfileManager.getCrateActiveList().contains(player.getUniqueId()) && !CrateProfileManager.getWasOfflineList().contains(player.getUniqueId())) {
      CrateProfileManager.addToWasOfflineList(player.getUniqueId());
    }
  }
}
