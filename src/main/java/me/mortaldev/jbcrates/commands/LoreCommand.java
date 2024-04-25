package me.mortaldev.jbcrates.commands;

import me.mortaldev.jbcrates.utils.CommandHandler;
import me.mortaldev.jbcrates.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LoreCommand {
    public LoreCommand() {
        new CommandHandler("lore", -1, true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType().equals(Material.AIR)) {
                    player.sendMessage(TextUtil.format("&cYou must have something in your hand."));
                    return true;
                }
                List<Component> newItemLore = new ArrayList<>();
                StringBuilder fullLore = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    if (i+1 == args.length) {
                        fullLore.append(args[i]);
                    } else {
                        fullLore.append(args[i]).append(" ");
                    }
                }

                String[] loreInput;

                if (args.length > 0) {
                    loreInput = fullLore.toString().split(";;");
                    for (String s : loreInput) {
                        newItemLore.add(TextUtil.format(s));
                    }
                }
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.lore(newItemLore);
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
