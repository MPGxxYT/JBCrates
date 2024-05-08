package me.mortaldev.jbcrates.configs;

import me.mortaldev.jbcrates.utils.YamlConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MainConfig {
  private Long crateCooldown;
  private Map<String, Double> worldWhitelistMap;
  FileConfiguration config;

  public MainConfig() {
    reload();
  }

  public String reload(){
    config = YamlConfig.getConfig("config");
    crateCooldown = config.getLong("crateCooldown");
    ConfigurationSection section = config.getConfigurationSection("worldWhitelist");
    if (section == null) {
      return YamlConfig.failedToLoad("main", "worldWhitelist");
    }
    Map<String, Object> values = section.getValues(false);
    worldWhitelistMap = new HashMap<>();
    values.forEach((string, object) -> worldWhitelistMap.put(string, Double.valueOf(object.toString())));
    return "Reloaded config!";
  }

  public Long getCrateCooldown() {
    return crateCooldown;
  }

  public Map<String, Double> getWorldWhitelistMap() {
    return worldWhitelistMap;
  }

  public FileConfiguration getConfig() {
    return config;
  }
}
