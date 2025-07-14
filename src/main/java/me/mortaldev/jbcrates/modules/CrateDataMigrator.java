package me.mortaldev.jbcrates.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.stream.Stream;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.configs.MainConfig;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.crate.Crate_Old;

/**
 * A utility class to perform a one-time migration of crate data from the old format (Crate_Old) to
 * the new format (Crate).
 */
public class CrateDataMigrator {

  /**
   * Runs the entire migration process. It's recommended to run this asynchronously to avoid
   * blocking the server's main thread.
   */
  public static void runMigration() {
    if (MainConfig.getInstance().isMigrated()) {
      return;
    }

    // It's likely your CRUD API already has a pre-configured ObjectMapper.
    // If so, you should use that instance to ensure Bukkit types are handled correctly.
    // For example: ObjectMapper mapper = CrateManager.getInstance().getMapper();
    ObjectMapper mapper = new ObjectMapper();
    // This allows Jackson to write the JSON in a human-readable format.
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    // You must configure your mapper to handle Bukkit objects like ItemStack.
    // This is often done with a custom module. Your CRUD API probably does this.

    // Define the paths for the old and new data directories.
    Path oldCratesPath = Main.getInstance().getDataFolder().toPath().resolve("crates_old");
    Path newCratesPath = Main.getInstance().getDataFolder().toPath().resolve("crates");

    File oldCratesDir = oldCratesPath.toFile();
    if (!oldCratesDir.exists() || !oldCratesDir.isDirectory()) {
      logInfo("&eMigration skipped: Old data directory not found at 'plugins/JBCrates/crates_old'");
      logInfo("&ePlease rename your original 'crates' folder to 'crates_old' to begin migration.");
      return;
    }

    File newCratesDir = newCratesPath.toFile();
    if (!newCratesDir.exists()) {
      if (!newCratesDir.mkdirs()) {
        logSevere("&cMigration failed: Could not create new data directory at " + newCratesPath);
        return;
      }
    }

    logInfo("&aStarting crate data migration from 'crates_old' to 'crates'...");
    int successCount = 0;
    int failCount = 0;

    // Use try-with-resources to ensure the stream is closed.
    try (Stream<Path> stream = Files.walk(oldCratesPath, 1)) {
      // Process each file ending with .json in the old directory.
      for (Path oldFilePath : stream.filter(path -> path.toString().endsWith(".json")).toList()) {
        File oldFile = oldFilePath.toFile();
        try {
          // 1. Deserialize the old JSON file into a Crate_Old object.
          Crate_Old oldCrate = mapper.readValue(oldFile, Crate_Old.class);

          // 2. Use the static factory method to convert it to the new Crate format.
          Crate newCrate = Crate.fromOldCrate(oldCrate);

          // 3. Serialize the new Crate object and save it to the new directory.
          CrateManager.getInstance().add(newCrate, true);

          logInfo("  &7- Successfully migrated: " + oldFile.getName());
          successCount++;

        } catch (Exception e) {
          logSevere("&c  - Failed to migrate file: " + oldFile.getName());
          Main.getInstance()
              .getLogger()
              .log(Level.SEVERE, "Migration error for " + oldFile.getName(), e);
          failCount++;
        }
      }
    } catch (IOException e) {
      logSevere("&cMigration failed: Could not read from the old data directory.");
      Main.getInstance().getLogger().log(Level.SEVERE, "Migration I/O error", e);
      return;
    }

    logInfo("&a-----------------------------------------");
    logInfo("&aCrate Data Migration Complete!");
    logInfo("&aSuccessfully migrated: " + successCount + " files.");
    logInfo("&cFailed to migrate: " + failCount + " files.");
    logInfo("&a-----------------------------------------");

    if (failCount == 0 && successCount > 0) {
      logInfo("&eYou can now safely delete the 'crates_old' folder if you have made a backup.");
    } else if (failCount > 0) {
      logWarning(
          "&ePlease review the console errors for the failed migrations before deleting any files.");
    }
    MainConfig.getInstance().setMigrated(true);
  }

  // Helper methods for colored console logging
  private static void logInfo(String message) {
    Main.getInstance().getLogger().info(message.replace('&', '§'));
  }

  private static void logWarning(String message) {
    Main.getInstance().getLogger().warning(message.replace('&', '§'));
  }

  private static void logSevere(String message) {
    Main.getInstance().getLogger().severe(message.replace('&', '§'));
  }
}
