package me.mortaldev.jbcrates;

import me.mortaldev.jbcrates.commands.JBCrateCommand;
import me.mortaldev.jbcrates.commands.LoreCommand;
import me.mortaldev.jbcrates.commands.RenameCommand;
import me.mortaldev.jbcrates.listeners.OnCratePlaceEvent;
import me.mortaldev.jbcrates.modules.menu.GUIListener;
import me.mortaldev.jbcrates.modules.menu.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public static Main instance;
    public static GUIManager guiManager;
    private static final String LABEL = "JBCrates";

    @Override
    public void onEnable() {
        instance = this;

        // DEPENDENCIES

//        if (Bukkit.getPluginManager().getPlugin("DecentHolograms") == null){
//            getLogger().warning("Could not find DecentHolograms! This plugin is required.");
//            Bukkit.getPluginManager().disablePlugin(this);
//            return;
//        }

        // GUIs
        guiManager = new GUIManager();

        GUIListener guiListener = new GUIListener(guiManager);
        Bukkit.getPluginManager().registerEvents(guiListener, this);

        // Listeners

        Bukkit.getPluginManager().registerEvents(new OnCratePlaceEvent(), this);

        // DATA FOLDER

        if (!getDataFolder().exists()){
            getDataFolder().mkdir();
        }

        new JBCrateCommand();
        new LoreCommand();
        new RenameCommand();

        getLogger().info(LABEL + " Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info(LABEL + " Disabled");
    }

    public static Main getInstance() {
        return instance;
    }
    public static String getLabel() {
        return LABEL;
    }

    public static GUIManager getGuiManager() {
        return guiManager;
    }
}


