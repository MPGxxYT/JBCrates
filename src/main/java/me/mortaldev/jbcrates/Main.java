package me.mortaldev.jbcrates;

import co.aikar.commands.PaperCommandManager;
import java.util.*;
import java.util.stream.Collectors;
import me.mortaldev.jbcrates.commands.GetCrateRewardsCommand;
import me.mortaldev.jbcrates.commands.JBCrateCommand;
import me.mortaldev.jbcrates.configs.MainConfig;
import me.mortaldev.jbcrates.listeners.OnCratePlaceEvent;
import me.mortaldev.jbcrates.listeners.OnJoinRewardsReminderEvent;
import me.mortaldev.jbcrates.listeners.OnPlayerQuitEvent;
import me.mortaldev.jbcrates.menus.CrateRewardsMenu;
import me.mortaldev.jbcrates.menus.ManageCrateMenu;
import me.mortaldev.jbcrates.menus.ViewCratesMenu;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.GUIListener;
import me.mortaldev.jbcrates.modules.menu.GUIManager;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

  private static final String LABEL = "JBCrates";
  public static Main instance;
  public static GUIManager guiManager;
  public static MainConfig mainConfig;
  static PaperCommandManager commandManager;
  //  static HashSet<String> dependencies = new HashSet<>();
  static List<Location> crateLocationList = new ArrayList<>();

  public static MainConfig getMainConfig() {
    return mainConfig;
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

    // Configs (must be first since a lot of things rely on the config)

    mainConfig = new MainConfig();

    CrateProfileManager.loadCrateProfileMap();
    CrateManager.updateCratesList();

    // DEPENDENCIES

    //    for (String plugin : dependencies) {
    //      if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
    //        getLogger().warning("Could not find " + plugin + "! This plugin is required.");
    //        Bukkit.getPluginManager().disablePlugin(this);
    //        return;
    //      }
    //    }

    // GUI Manager
    guiManager = new GUIManager();
    GUIListener guiListener = new GUIListener(guiManager);
    Bukkit.getPluginManager().registerEvents(guiListener, this);

    // Listeners/Events

    Bukkit.getPluginManager().registerEvents(new OnCratePlaceEvent(), this);
    Bukkit.getPluginManager().registerEvents(new OnJoinRewardsReminderEvent(), this);
    Bukkit.getPluginManager().registerEvents(new OnPlayerQuitEvent(), this);

    Bukkit.getPluginManager().registerEvents(new ViewCratesMenu.AddCratePromptEvent(), this);
    Bukkit.getPluginManager().registerEvents(new ManageCrateMenu.SetCrateNamePromptEvent(), this);
    Bukkit.getPluginManager().registerEvents(new CrateRewardsMenu.SetRewardNamePrompt(), this);

    // Commands

    commandManager
        .getCommandCompletions()
        .registerCompletion(
            "crates",
            c -> CrateManager.getCrates().stream().map(Crate::getId).collect(Collectors.toSet()));

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

    CrateProfileManager.saveCrateProfileMap();
  }
}
