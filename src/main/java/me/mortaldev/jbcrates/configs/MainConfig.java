package me.mortaldev.jbcrates.configs;

import java.util.HashMap;
import java.util.Map;
import me.mortaldev.AbstractConfig;
import me.mortaldev.ConfigValue;
import me.mortaldev.jbcrates.Main;
import org.bukkit.plugin.java.JavaPlugin;

public class MainConfig extends AbstractConfig {
  private static class Singleton {
    private static final MainConfig INSTANCE = new MainConfig();
  }

  public static MainConfig getInstance() {
    return Singleton.INSTANCE;
  }

  private MainConfig() {}

  private final ConfigValue<Integer> crateCooldown = new ConfigValue<>("crateCooldown", 20000);
  private final ConfigValue<Map<String, Integer>> worldWhitelist =
      new ConfigValue<>(
          "worldWhitelist",
          new HashMap<>() {
            {
              put("world", 125);
            }
          });
  private final ConfigValue<Boolean> migrated = new ConfigValue<>("migrated", false);

  @Override
  public void log(String message) {
    Main.log(message);
  }

  @Override
  public String getName() {
    return "config";
  }

  @Override
  public JavaPlugin getMain() {
    return Main.getInstance();
  }

  @Override
  public void loadData() {
    crateCooldown.setValue(getConfigValue(crateCooldown).getValue());
    worldWhitelist.setValue(getConfigValue(worldWhitelist).getValue());
    migrated.setValue(getConfigValue(migrated).getValue());
  }

  public boolean isMigrated() {
    return migrated.getValue();
  }

  public void setMigrated(boolean bool) {
    migrated.setValue(bool);
    saveValue(migrated);
  }

  public Integer getCrateCooldown() {
    return crateCooldown.getValue();
  }

  public Map<String, Integer> getWorldWhitelist() {
    return worldWhitelist.getValue();
  }
}
