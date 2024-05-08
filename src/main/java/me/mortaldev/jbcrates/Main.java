package me.mortaldev.jbcrates;

import me.mortaldev.jbcrates.commands.GetCrateRewardsCommand;
import me.mortaldev.jbcrates.commands.JBCrateCommand;
import me.mortaldev.jbcrates.configs.MainConfig;
import me.mortaldev.jbcrates.listeners.OnCratePlaceEvent;
import me.mortaldev.jbcrates.listeners.OnJoinRewardsReminderEvent;
import me.mortaldev.jbcrates.listeners.OnPlayerQuitEvent;
import me.mortaldev.jbcrates.menus.CrateRewardsMenu;
import me.mortaldev.jbcrates.menus.ManageCrateMenu;
import me.mortaldev.jbcrates.menus.ViewCratesMenu;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.GUIListener;
import me.mortaldev.jbcrates.modules.menu.GUIManager;
import me.mortaldev.jbcrates.modules.profile.CrateProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin {

  public static Main instance;
  public static GUIManager guiManager;
  public static MainConfig mainConfig;
  static List<Location> crateLocationList = new ArrayList<>();
  private static final String LABEL = "JBCrates";

  public void addCrateLocation(Location location) {
    crateLocationList.add(location);
  }

  public void removeCrateLocation(Location location) {
    crateLocationList.remove(location);
  }

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

  @Override
  public void onEnable() {
    instance = this;

    // Configs (must be first since a lot of things rely on the config)

    mainConfig = new MainConfig();

    CrateProfileManager.loadCrateProfileMap();
    CrateManager.updateCratesList();

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
    Bukkit.getPluginManager().registerEvents(new OnJoinRewardsReminderEvent(), this);
    Bukkit.getPluginManager().registerEvents(new OnPlayerQuitEvent(), this);

    Bukkit.getPluginManager().registerEvents(new ViewCratesMenu.AddCratePromptEvent(), this);
    Bukkit.getPluginManager().registerEvents(new ManageCrateMenu.SetCrateNamePromptEvent(), this);
    Bukkit.getPluginManager().registerEvents(new CrateRewardsMenu.SetRewardNamePrompt(), this);

    // DATA FOLDER

    if (!getDataFolder().exists()) {
      getDataFolder().mkdir();
    }

    // Commands

    new JBCrateCommand();
    new GetCrateRewardsCommand();
    // new LoreCommand();
    // new RenameCommand();

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
