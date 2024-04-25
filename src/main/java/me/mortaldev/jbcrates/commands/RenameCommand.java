package me.mortaldev.jbcrates.commands;

import me.mortaldev.jbcrates.utils.CommandHandler;
import me.mortaldev.jbcrates.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LoreTempCommand {
    public LoreTempCommand() {
        new CommandHandler("lore", -1, true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                List<Component> newItemLore = new ArrayList<>();
                String[] loreInput = new String[0];

                if (args.length > 0) {
                    loreInput = args[0].split(";;");
                }
                for (String s : loreInput) {
                    newItemLore.add(TextUtil.format(s));
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
