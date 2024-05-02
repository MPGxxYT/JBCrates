package me.mortaldev.jbcrates.commands;

import me.mortaldev.jbcrates.modules.profile.CrateProfile;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import me.mortaldev.jbcrates.utils.CommandHandler;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GetCrateRewardsCommand {

  public GetCrateRewardsCommand() {
    new CommandHandler("getCrateRewards", -1, true) {
      @Override
      public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        if (!CrateProfileManager.hasCrateProfile(player.getUniqueId())) {
          player.sendMessage(TextUtil.format("&cYou do not have any rewards to claim."));
          return true;
        }
        if (CrateProfileManager.getCrateActiveList().contains(player.getUniqueId()) && !CrateProfileManager.getWasOfflineList().contains(player.getUniqueId())) {
          player.sendMessage(TextUtil.format("&cYou do not have any rewards to claim."));
          return true;
        }
        CrateProfile profile = CrateProfileManager.getCrateProfile(player.getUniqueId());
        List<ItemStack> itemList = profile.getItemList();
        if (!Utils.canInventoryHold(player.getInventory(), itemList.size())) {
          player.sendMessage(TextUtil.format("&cYou do not have enough space. Make " + itemList.size() + " empty slot spaces."));
          return true;
        }
        for (ItemStack itemStack : itemList) {
          player.getInventory().addItem(itemStack);
          Component append = itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().displayName() : TextUtil.format(Utils.itemName(itemStack));
          player.sendMessage(TextUtil.format("&6You received ").append(append));
          CrateProfileManager.removeCrateProfile(player.getUniqueId());
        }
        return true;
      }

      @Override
      public @NotNull List<String> onTabComplete(
          @NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
      }

      @Override
      public @NotNull String getUsage() {
        return "/getCrateRewards";
      }

      @Override
      public String getPermission() {
        return "jbcrates.getcraterewards";
      }

      @Override
      public @NotNull String getDescription() {
        return "Will give you any crate rewards you may have not gotten.";
      }

      @Override
      public @NotNull List<String> getAliases() {
        return new ArrayList<>();
      }
    };
  }
}
