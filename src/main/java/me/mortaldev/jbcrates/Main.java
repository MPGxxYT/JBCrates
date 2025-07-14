package me.mortaldev.jbcrates;

import co.aikar.commands.PaperCommandManager;
import java.util.*;
import java.util.stream.Collectors;
import me.mortaldev.crudapi.loading.CRUDRegistry;
import me.mortaldev.jbcrates.commands.GetCrateRewardsCommand;
import me.mortaldev.jbcrates.commands.JBCrateCommand;
import me.mortaldev.jbcrates.configs.MainConfig;
import me.mortaldev.jbcrates.listeners.ChatListener;
import me.mortaldev.jbcrates.listeners.OnCratePlaceEvent;
import me.mortaldev.jbcrates.modules.CrateDataMigrator;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.menuapi.GUIListener;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

  private static final String LABEL = "JBCrates";
  public static Main instance;
  static PaperCommandManager commandManager;
  //  static HashSet<String> dependencies = new HashSet<>();
  static List<Location> crateLocationList = new ArrayList<>();

  public static Main getInstance() {
    return instance;
  }

  public static String getLabel() {
    return LABEL;
  }

  public static void log(String message) {
    Bukkit.getLogger().info("[" + Main.getLabel() + "] " + message);
  }

  public void addCrateLocation(Location location) {
    crateLocationList.add(location);
  }

  public void removeCrateLocation(Location location) {
    crateLocationList.remove(location);
  }

  @Override
  public void onEnable() {
    instance = this;
    commandManager = new PaperCommandManager(this);

    // DATA FOLDER

    if (!getDataFolder().exists()) {
      getDataFolder().mkdir();
    }

    // Configs
    MainConfig.getInstance().load();

    CrateDataMigrator.runMigration();

    // Managers
    CRUDRegistry.getInstance()
        .scanAndRegister(
            this.getClass().getClassLoader(),
            "me.mortaldev.jbcrates"); // example: me.mortaldev.jbjuly4th
    CRUDRegistry.getInstance().initialize();

    // GUI Manager
    GUIListener guiListener = new GUIListener(GUIManager.getInstance()); // MenuAPI
    Bukkit.getPluginManager().registerEvents(guiListener, this);

    // Listeners/Events

    Bukkit.getPluginManager().registerEvents(new OnCratePlaceEvent(), this);
    Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

    // Commands

    commandManager
        .getCommandCompletions()
        .registerCompletion(
            "crates",
            c ->
                CrateManager.getInstance().getSet().stream()
                    .map(Crate::getId)
                    .collect(Collectors.toSet()));

    commandManager.registerCommand(new JBCrateCommand());
    commandManager.registerCommand(new GetCrateRewardsCommand());

    getLogger().info(LABEL + " Enabled");
  }

  @Override
  public void onDisable() {
    getLogger().info(LABEL + " Disabled");

    // Sets all active crates to air on restart.
    if (!crateLocationList.isEmpty()) {
      for (Location location : crateLocationList) {
        location.getBlock().setType(Material.AIR);
        for (int i = 0; i < 2; i++) {
          location.subtract(0, 1, 0).getBlock().setType(Material.AIR);
        }
      }
    }
  }
}
