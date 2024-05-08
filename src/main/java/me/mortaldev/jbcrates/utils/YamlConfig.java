package me.mortaldev.jbcrates.utils;

import me.mortaldev.jbcrates.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Objects;

public class YamlConfig {

  public static String failedToLoad(String configName, String configValue){
    return failedToLoad(configName, configValue, "INVALID VALUE");
  }
  public static String failedToLoad(String configName, String configValue, String failReason){
    String message = MessageFormat.format("[{0}.YML] Failed to load config value: {1} ({2})", configName, configValue, failReason);
    Main.getInstance().getLogger().warning(message);
    YamlConfig.loadResource(configName);
    return message;
  }

  public static FileConfiguration createNewConfig(String name){
    if (!name.contains(".yml")){
      name = name.concat(".yml");
    }
    File file = new File(Main.getInstance().getDataFolder(), name);
    if (file.exists()){
      return getConfig(name);
    }
    try {
      file.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return YamlConfiguration.loadConfiguration(file);
  }

  public static FileConfiguration getConfig(String name) {
    if (!name.contains(".yml")){
      name = name.concat(".yml");
    }
    File file = new File(Main.getInstance().getDataFolder(), name);
    if (!file.exists()){
      loadResource(name);
    }
    return YamlConfiguration.loadConfiguration(file);
  }

  public static FileConfiguration getOtherConfig(File file) {
    if (!file.exists()){
      Main.getInstance().getLogger().warning("Error finding other config: "+ file);
    }
    return YamlConfiguration.loadConfiguration(file);
  }

  public static void saveOtherConfig(File file, FileConfiguration config){
    try {
      config.save(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void saveConfig(FileConfiguration config, String configName){
    if (!configName.contains(".yml")){
      configName = configName.concat(".yml");
    }
    File file = new File(Main.getInstance().getDataFolder(), configName);
    try {
      config.save(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void loadResource(String name){
    if (!name.contains(".yml")){
      name = name.concat(".yml");
    }
    InputStream stream = Main.getInstance().getResource(name);
    if (stream == null){
      Main.getInstance().getLogger().warning("Failed to load resource: " + name);
      return;
    }
    File file = new File(Main.getInstance().getDataFolder(), name);
    try {
      if (!file.exists()){
        file.createNewFile();
      }
      OutputStream outputStream = new FileOutputStream(file);
      outputStream.write(stream.readAllBytes());
      outputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void saveDefaultConfig(FileConfiguration config){
    Main.getInstance().saveResource(config.getName(), false);
  }

  public static void reloadConfig(FileConfiguration config) {
    config = YamlConfiguration.loadConfiguration(new File(config.getCurrentPath()));
    Reader stream = new InputStreamReader(Objects.requireNonNull(Main.getInstance().getResource(config.getName())), StandardCharsets.UTF_8);
    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(stream);
    config.setDefaults(defConfig);
  }
}