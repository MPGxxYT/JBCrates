package me.mortaldev.jbcrates.modules.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LogManager {
  private static final String PATH = Main.getInstance().getDataFolder() + "/logs/";
  public static final String LOG_PERM = "jbcrates.log";
  public static final String FOLDER_FORMAT = "MMM-d";
  public static final String LOG_EXTENSION = ".log";

  public static void logToFile(Log log) {
    String fileName = log.getDateTime().format(DateTimeFormatter.ofPattern(FOLDER_FORMAT));
    String data = log.formFileLog();
    Main.log(log.formConsoleLog());
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!player.hasPermission(LOG_PERM)) {
        continue;
      }
      player.sendMessage(TextUtil.format(log.formChatLog()));
    }
    File dir = new File(PATH);
    File file = new File(dir, fileName + LOG_EXTENSION);
    dir.mkdirs();
    try {
      if (!file.exists()) {
        file.createNewFile();
      }
      BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
      writer.write(data);
      writer.newLine();
      writer.close();
    } catch (IOException e) {
      Bukkit.getLogger().warning(e.getMessage());
    }
  }
}
