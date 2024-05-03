package me.mortaldev.jbcrates.commands;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.menus.InitialCratesMenu;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.utils.CommandHandler;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JBCrateCommand {
  public JBCrateCommand() {
    new CommandHandler("jbcrates", -1, false) {
      @Override
      public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = null;
        if (sender instanceof Player player1){
          player = player1;
        }
        switch (args.length) {
          case 0 -> {
            if (player == null){
              sender.sendMessage("Console cannot open menus.");
              sender.sendMessage("/jbcrates give <player> <crate_id>");
              return true;
            }
            Main.getGuiManager().openGUI(new InitialCratesMenu(), player);
          }
          case 1 -> {
            if (args[0].equalsIgnoreCase("give")) {
              if (player == null){
                sender.sendMessage("Player input a player.");
                sender.sendMessage("/jbcrates give <player> <crate_id>");
              } else {
                sender.sendMessage(TextUtil.format("&7/jbcrates give &c<player> &7<crate_id>"));
              }
            } else {
              if (player == null){
                sender.sendMessage("/jbcrates give <player> <crate_id>");
              } else {
                sender.sendMessage(TextUtil.format("&7/jbcrates &cgive &7<player> <crate_id>"));
              }
            }
          }
          case 2 -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (args[0].equalsIgnoreCase("give") && !offlinePlayer.isOnline()) {
              if (player == null){
                sender.sendMessage("Player is not online");
              } else {
                sender.sendMessage(TextUtil.format("&cPlayer is not online"));
              }
            }
          }
          case 3 -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (args[0].equalsIgnoreCase("give")) {
              if (!offlinePlayer.isOnline()){
                if (player == null){
                  sender.sendMessage("Player is not online");
                } else {
                  sender.sendMessage(TextUtil.format("&cPlayer is not online"));
                }
                return true;
              }
              if (!CrateManager.crateByIDExists(args[2])) {
                if (player == null){
                  sender.sendMessage("Crate by that id does not exist.");
                } else {
                  sender.sendMessage(TextUtil.format("&cCrate by that id does not exist."));
                }
                return true;
              }
              Crate crate = CrateManager.getCrate(args[2]);
              if (crate == null) {
                if (player == null){
                  sender.sendMessage("Failed to get crate by that id.");
                } else {
                  sender.sendMessage(TextUtil.format("&cFailed to get crate by that id."));
                }
                return true;
              }
              if (crate.getAmountToWin() > crate.getRewardsMap().size()){
                if (player == null){
                  sender.sendMessage("CRATE INCORRECTLY CONFIGURED: amountToWin > totalItems (" + crate.getId() + ")");
                } else {
                  player.sendMessage(TextUtil.format("&cCRATE INCORRECTLY CONFIGURED: amountToWin > totalItems &7(" + crate.getId() + ")"));
                }
                return true;
              }
              ItemStack itemStack = CrateManager.generatePlaceCrateItemStack(crate);
              offlinePlayer.getPlayer().getInventory().addItem(itemStack);
              Bukkit.getLogger().info("Gave " + offlinePlayer.getName() + " a crate: " + crate.getId());
            }
          }
        }

        return true;
      }

      @Override
      public @NotNull List<String> onTabComplete(
          @NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> list = new ArrayList<>();
        switch (args.length) {
          case 1 -> list.add("give");
          case 2 -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
              list.add(player.getName());
            }
          }
          case 3 -> {
            for (Crate crate : CrateManager.getCrates()) {
              list.add(crate.getId());
            }
          }
        }
        return list;
      }

      @Override
      public @NotNull String getUsage() {
        return "/jbcrates";
      }

      @Override
      public String getPermission() {
        return "crates.admin";
      }

      @Override
      public @NotNull String getDescription() {
        return "Create, edit and delete your crates.";
      }

      @Override
      public @NotNull List<String> getAliases() {
        return new ArrayList<>() {
          {
            add("crates");
          }
        };
      }
    };
  }
}
