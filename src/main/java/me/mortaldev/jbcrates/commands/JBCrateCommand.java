package me.mortaldev.jbcrates.commands;

import me.mortaldev.jbcrates.menus.InitialCratesMenu;
import me.mortaldev.jbcrates.utils.CommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JBCrateCommand {
    public JBCrateCommand() {
        new CommandHandler("jbcrates", -1, true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
                Player player = (Player) sender;
                player.openInventory(new InitialCratesMenu().createInventory(player));
                return true;
            }

            @Override
            public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
                return new ArrayList<>();
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
                return new ArrayList<>(){{
                    add("crates");
                }};
            }
        };
    }
}
