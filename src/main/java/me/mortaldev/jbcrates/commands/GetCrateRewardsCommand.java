package me.mortaldev.jbcrates.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.profile.CrateProfile;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import me.mortaldev.jbcrates.utils.NBTAPI;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@CommandAlias("getCrateRewards|crateRewards")
@CommandPermission("jbcrates.getcraterewards")
public class GetCrateRewardsCommand extends BaseCommand {

  @Default
  public void onDefault(Player player) {
    if (!CrateProfileManager.hasCrateProfile(player.getUniqueId())) {
      player.sendMessage(TextUtil.format("&cYou do not have any rewards to claim."));
      return;
    }
    if (CrateProfileManager.getCrateActiveList().contains(player.getUniqueId()) && !CrateProfileManager.getWasOfflineList().contains(player.getUniqueId())) {
      player.sendMessage(TextUtil.format("&cYou do not have any rewards to claim."));
      return;
    }
    CrateProfile profile = CrateProfileManager.getCrateProfile(player.getUniqueId());
    List<ItemStack> itemList = profile.getItemList();
    if (!Utils.canInventoryHold(player.getInventory(), itemList.size())) {
      player.sendMessage(TextUtil.format("&cYou do not have enough space. Make " + itemList.size() + " empty slot spaces."));
      return;
    }
    for (ItemStack itemStack : itemList) {
      if (NBTAPI.hasNBT(itemStack, "commandReward")) {
        String commandReward = NBTAPI.getNBT(itemStack, "commandReward");
        if (commandReward == null) {
          Main.log("Error has occurred: Command not found in commandReward ("+itemStack.getType().name()+")");
          return;
        }
        if (commandReward.startsWith("/")) {
          commandReward = commandReward.replaceFirst("/", "");
        }
        commandReward = commandReward.replaceAll("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandReward);
      } else {
        player.getInventory().addItem(itemStack);
      }
      Component append = itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().displayName() : TextUtil.format(Utils.itemName(itemStack));
      if (append == null) {
        append = TextUtil.format("");
      }
      player.sendMessage(TextUtil.format("&6You received &f").append(append));
    }
    CrateProfileManager.removeCrateProfile(player.getUniqueId());

  }
}
