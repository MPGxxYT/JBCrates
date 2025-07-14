package me.mortaldev.jbcrates.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import java.util.ArrayList;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.profile.CrateProfile;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import me.mortaldev.jbcrates.utils.NBTAPI;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("getCrateRewards|crateRewards")
@CommandPermission("jbcrates.getcraterewards")
public class GetCrateRewardsCommand extends BaseCommand {

  @Default
  public void onDefault(Player player) {
    CrateProfile crateProfile =
        CrateProfileManager.getInstance()
            .getByID(player.getUniqueId().toString())
            .orElse(CrateProfile.create(player.getUniqueId().toString()));
    if (crateProfile.getOverflowRewards().isEmpty()) {
      player.sendMessage(TextUtil.format("&cYou do not have any rewards to claim."));
      return;
    }
    boolean failedToFit = false;
    for (ItemStack itemStack : new ArrayList<>(crateProfile.getOverflowRewards())) {
      if (NBTAPI.hasNBT(itemStack, "commandReward")) {
        String commandReward = NBTAPI.getNBT(itemStack, "commandReward");
        if (commandReward == null) {
          Main.log(
              "Error has occurred: Command not found in commandReward ("
                  + itemStack.getType().name()
                  + ")");
          return;
        }
        if (commandReward.startsWith("/")) {
          commandReward = commandReward.replaceFirst("/", "");
        }
        commandReward = commandReward.replaceAll("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReward);
      } else {
        if (!Utils.canInventoryHold(player.getInventory(), itemStack)) {
          failedToFit = true;
        } else {
          player.getInventory().addItem(itemStack);
          crateProfile.removeItem(itemStack);
        }
      }
    }
    player.sendMessage(TextUtil.format("&3You have claimed your rewards!"));
    if (failedToFit) {
      CrateProfileManager.getInstance().update(crateProfile, true);
      player.sendMessage(TextUtil.format("&cFailed to fit all items in inventory."));
      player.sendMessage(TextUtil.format("&cMake space and retry."));
    } else {
      CrateProfileManager.getInstance().remove(crateProfile);
    }
  }
}
