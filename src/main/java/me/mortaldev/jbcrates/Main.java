package me.mortaldev.jbcrates;

import me.mortaldev.jbcrates.commands.JBCrateCommand;
import me.mortaldev.jbcrates.commands.LoreCommand;
import me.mortaldev.jbcrates.commands.RenameCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public static Main instance;
    private static final String LABEL = "JBCrates";

    @Override
    public void onEnable() {
        instance = this;
        //getServer().getPluginManager().registerEvents();

        new JBCrateCommand();
        new LoreCommand();
        new RenameCommand();
    }

    public static Main getInstance() {
        return instance;
    }
    public static String getLabel() {
        return LABEL;
    }
}


