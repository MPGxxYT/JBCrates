package me.mortaldev.jbcrates.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.mortaldev.jbcrates.configs.MainConfig;
import me.mortaldev.jbcrates.menus.CratesMenu;
import me.mortaldev.jbcrates.menus.MenuData;
import me.mortaldev.jbcrates.modules.crate.CrateHandler;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.log.Log;
import me.mortaldev.jbcrates.modules.log.LogManager;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("jbcrates|crates|crate")
public class JBCrateCommand extends BaseCommand {
  @Default
  @CommandPermission("jbcrates.admin")
  public void openMenu(Player player) {
    GUIManager.getInstance().openGUI(new CratesMenu(new MenuData()), player);
  }

  @Subcommand("reload")
  @CommandPermission("jbcrates.admin")
  public void reload(CommandSender sender) {
    Player player = sender instanceof Player ? ((Player) sender) : null;
    if (player == null) {
      sender.sendMessage("Reloading config...");
    } else {
      sender.sendMessage(TextUtil.format("&eReloading config..."));
    }
    String reload = MainConfig.getInstance().reload();
    sender.sendMessage(reload);
  }

  public void send(CommandSender sender, String message) {
    if (sender instanceof Player player) {
      player.sendMessage(TextUtil.format(message));
    } else {
      String cleanedMessage = TextUtil.removeDecorAndColor(message);
      sender.sendMessage(cleanedMessage);
    }
  }

  @Subcommand("give")
  @CommandCompletion("@players @crates @nothing")
  @CommandPermission("jbcrates.admin|jbcrates.give")
  @Syntax("<player> <crate id>")
  public void give(CommandSender sender, OfflinePlayer receiver, String crateID) {
    CrateManager.getInstance()
        .getByID(crateID)
        .ifPresentOrElse(
            crate -> {
              ItemStack crateItemStack =
                  CrateHandler.getInstance().generatePlaceCrateItemStack(crate);
              if (receiver == null) {
                send(sender, "&cThat user does not exist.");
              } else if (!receiver.isOnline() || receiver.getPlayer() == null) {
                //
                // CrateProfileManager.getCrateProfile(receiver.getUniqueId()).addItem(crateItemStack);
                LogManager.logToFile(
                    new Log(Log.Status.OFFLINE, sender.getName(), receiver.getName(), crateID));
              } else {
                // Player is online, added to inventory.
                if (Utils.canInventoryHold(receiver.getPlayer().getInventory(), crateItemStack)) {
                  receiver.getPlayer().getInventory().addItem(crateItemStack);
                  LogManager.logToFile(
                      new Log(Log.Status.RECEIVED, sender.getName(), receiver.getName(), crateID));
                } else {
                  // Player is online, cannot hold, add to rewards claim.
                  //                  CrateProfileManager.getCrateProfile(receiver.getUniqueId())
                  //                      .addItem(crateItemStack);
                  LogManager.logToFile(
                      new Log(Log.Status.RECEIVED, sender.getName(), receiver.getName(), crateID));
                }
              }
            },
            () -> {
              send(sender, "&cThat crate does not exist.");
              LogManager.logToFile(
                  new Log(Log.Status.FAILED, sender.getName(), receiver.getName(), crateID));
            });
  }
}
