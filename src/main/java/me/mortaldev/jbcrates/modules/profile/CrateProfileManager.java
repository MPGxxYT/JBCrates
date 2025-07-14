package me.mortaldev.jbcrates.modules.profile;

import java.util.Arrays;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.menus.edit.MoreOptionsRewardMenu;
import me.mortaldev.jbcrates.utils.NBTAPI;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CrateProfileManager extends CRUDManager<CrateProfile> {
  private static class Singleton {
    private static final CrateProfileManager INSTANCE = new CrateProfileManager();
  }

  public static CrateProfileManager getInstance() {
    return Singleton.INSTANCE;
  }

  private CrateProfileManager() {}

  @Override
  public CRUD<CrateProfile> getCRUD() {
    return CrateProfileCRUD.getInstance();
  }

  @Override
  public void log(String string) {
    Main.log(string);
  }

  public void giveItems(ItemStack[] itemStacks, OfflinePlayer offlinePlayer) {
    CrateProfile crateProfile =
        getByID(offlinePlayer.getUniqueId().toString())
            .orElse(CrateProfile.create(offlinePlayer.getUniqueId().toString()));
    Player player = offlinePlayer.getPlayer();
    if (player == null || !offlinePlayer.isOnline()) {
      Arrays.stream(itemStacks).forEach(crateProfile::addItem);
      return;
    }
    boolean failedToFit = false;
    player.sendMessage(TextUtil.format("&3You have received your crate rewards!"));
    for (ItemStack itemStack : itemStacks) {
      if (NBTAPI.hasNBT(itemStack, MoreOptionsRewardMenu.COMMAND_REWARD_KEY)) {
        String command = NBTAPI.getNBT(itemStack, MoreOptionsRewardMenu.COMMAND_REWARD_KEY);
        if (command == null) {
          continue;
        }
        command = command.replaceAll("%player%", player.getName());
        command = command.replaceFirst("/", "");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        continue;
      }
      if (!Utils.canInventoryHold(player.getInventory(), itemStack)) {
        failedToFit = true;
        crateProfile.addItem(itemStack);
      } else {
        player.getInventory().addItem(itemStack);
      }
    }
    if (failedToFit) {
      update(crateProfile, true);
      player.sendMessage(TextUtil.format("&cFailed to fit all items in inventory."));
      player.sendMessage(TextUtil.format("&cUse /getCrateRewards to claim your rewards."));
    }
  }
}
