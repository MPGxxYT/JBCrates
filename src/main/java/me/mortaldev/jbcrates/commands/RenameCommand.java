package me.mortaldev.jbcrates.commands;

import java.util.ArrayList;
import java.util.List;
import me.mortaldev.jbcrates.utils.CommandHandler;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class RenameCommand {
    public RenameCommand() {
        new CommandHandler("rename", -1, true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType().equals(Material.AIR)) {
                    player.sendMessage(TextUtil.format("&cYou must have something in your hand."));
                    return true;
                }

                ItemMeta itemMeta = item.getItemMeta();

                if (args.length > 0) {
                    StringBuilder newName = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        if (i+1 == args.length) {
                            newName.append(args[i]);
                        } else {
                            newName.append(args[i]).append(" ");
                        }
                    }
                    itemMeta.displayName(TextUtil.format(newName.toString()));
                }
                item.setItemMeta(itemMeta);
                return true;
            }

            @Override
            public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
                return new ArrayList<>();
            }

            @Override
            public @NotNull String getUsage() {
                return "null";
            }

            @Override
            public String getPermission() {
                return "*";
            }

            @Override
            public @NotNull String getDescription() {
                return "null";
            }

            @Override
            public @NotNull List<String> getAliases() {
                return new ArrayList<>();
            }
        };
    }
}
