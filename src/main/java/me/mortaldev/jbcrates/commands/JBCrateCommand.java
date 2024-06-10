package me.mortaldev.jbcrates.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.menus.InitialCratesMenu;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.log.Log;
import me.mortaldev.jbcrates.modules.log.LogManager;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@CommandAlias("jbcrates|crates|crate")
public class JBCrateCommand extends BaseCommand {
  @Default
  @CommandPermission("jbcrates.admin")
  public void openMenu(Player player){
    Main.getGuiManager().openGUI(new InitialCratesMenu(), player);
  }

  @Subcommand("reload")
  @CommandPermission("jbcrates.admin")
  public void reload(CommandSender sender){
    Player player = sender instanceof Player ? ((Player) sender) : null;
    if (player == null){
      sender.sendMessage("Reloading config...");
    } else {
      sender.sendMessage(TextUtil.format("&eReloading config..."));
    }
    String reload = Main.getMainConfig().reload();
    sender.sendMessage(reload);
  }

  @Subcommand("give")
  @CommandCompletion("@players @crates")
  @CommandPermission("jbcrates.admin|jbcrates.give")
  public void give(CommandSender sender, String[] args){
    Player player = sender instanceof Player ? ((Player) sender) : null;
    switch (args.length) {
      case 0 -> {
        if (player == null) {
          sender.sendMessage("Input a player.");
          sender.sendMessage("/crates give <player> <crate id>");
        } else {
          sender.sendMessage(TextUtil.format("&7/crates give &c<player>&7 <crate id>"));
        }
      }
      case 1 -> {
        if (player == null) {
          sender.sendMessage("Input a crate.");
          sender.sendMessage("/crates give <player> <crate id>");
        } else {
          sender.sendMessage(TextUtil.format("&7/crates give &7<player>&c <crate id>"));
        }
      }
      case 2 -> {
        OfflinePlayer receiver = Bukkit.getOfflinePlayer(args[0]);
        if (!Arrays.stream(Bukkit.getOfflinePlayers()).toList().contains(receiver)) {
          if (player == null) {
            sender.sendMessage("That player has never joined.");
          } else {
            sender.sendMessage(TextUtil.format("&cThat player has never joined."));
          }
          LogManager.logToFile(new Log(Log.Status.FAILED, sender.getName(), args[0], args[1]));
          return;
        }
        Crate crate = CrateManager.getCrate(args[1]);
        if (crate == null) {
          if (player == null) {
            sender.sendMessage("That crate does not exist.");
          } else {
            sender.sendMessage(TextUtil.format("&cThat crate does not exist."));
          }
          LogManager.logToFile(new Log(Log.Status.FAILED, sender.getName(), args[0], args[1]));
          return;
        }
        ItemStack crateItemStack = CrateManager.generatePlaceCrateItemStack(crate);
        if (receiver.isOnline()) {
          // Player is online, added to inventory.
          if (Utils.canInventoryHold(receiver.getPlayer().getInventory(), 1)) {
            receiver.getPlayer().getInventory().addItem(crateItemStack);
            LogManager.logToFile(new Log(Log.Status.RECEIVED, sender.getName(), args[0], args[1]));
          } else {
            // Player is online, cannot hold, add to rewards claim.
            CrateProfileManager.getCrateProfile(receiver.getUniqueId()).addItem(crateItemStack);
            LogManager.logToFile(new Log(Log.Status.RECEIVED, sender.getName(), args[0], args[1]));
          }
        } else {
          // Player is not online, add to rewards claim.
          CrateProfileManager.getCrateProfile(receiver.getUniqueId()).addItem(crateItemStack);
          LogManager.logToFile(new Log(Log.Status.OFFLINE, sender.getName(), args[0], args[1]));
        }
      }
    }
  }
}
