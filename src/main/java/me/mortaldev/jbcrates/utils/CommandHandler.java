package me.mortaldev.jbcrates.utils;

import me.mortaldev.jbcrates.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandHandler extends BukkitCommand implements CommandExecutor {
    private List<String> delayedPlayers = null;
    private int delay = 0;
    private final int minArguments;
    private final int maxArguments;
    private final boolean playerOnly;
    public CommandHandler(@NotNull String command) {
        this(command, 0);
    }
    public CommandHandler(@NotNull String command, boolean playerOnly) {
        this(command, 0, playerOnly);
    }
    public CommandHandler(@NotNull String command, int requiredArguments) {
        this(command, requiredArguments, requiredArguments);
    }
    public CommandHandler(@NotNull String command, int minArguments, int maxArguments) {
        this(command, minArguments, maxArguments, false);
    }
    public CommandHandler(@NotNull String command, int requiredArguments, boolean playerOnly) {
        this(command, requiredArguments, requiredArguments, playerOnly);
    }
    public CommandHandler(@NotNull String command, int minArguments, int maxArguments, boolean playerOnly) {
        super(command);

        this.minArguments = minArguments;
        this.maxArguments = maxArguments;
        this.playerOnly = playerOnly;

        this.setDescription(getDescription());
        this.setAliases(getAliases());
        this.setUsage(getUsage());
        this.setPermission(getPermission());

        CommandMap commandMap = getCommandMap();
        if (commandMap != null) {
            commandMap.register(Main.getLabel(), this);
            // "/command:command" ex "/fly:fly"
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return onTabComplete(sender, alias, args);
    }

    public CommandMap getCommandMap() {
        return Bukkit.getServer().getCommandMap();
    }

    public CommandHandler enableDelay(int delay) {
        this.delay = delay;
        this.delayedPlayers = new ArrayList<>();
        return this;
    }

    public void removeDelay(Player player) {
        this.delayedPlayers.remove(player.getName());
    }

    public void sendUsage(CommandSender sender) {
        sender.sendMessage(this.getUsage());
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String alias, String [] arguments) {
        if (arguments.length < minArguments || (arguments.length > maxArguments && maxArguments != -1)) {
            sendUsage(sender);
            return true;
        }

        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");

            return true;
        }

        String permission = this.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (delayedPlayers != null && sender instanceof Player player) {
            if (delayedPlayers.contains(player.getName())) {
                sender.sendMessage("§cPlease wait before using this command again.");
                return true;
            }

            delayedPlayers.add(player.getName());
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> delayedPlayers.remove(player.getName()), delay);
        }

        if (!onCommand(sender, arguments)) {
            sendUsage(sender);
        }

        return true;


    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return this.onCommand(sender, args);
    }

    public abstract boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args);

    public abstract @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args);
    public abstract @NotNull String getUsage();
    public abstract String getPermission();
    public abstract @NotNull String getDescription();
    public abstract @NotNull List<String> getAliases();
}