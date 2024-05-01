package me.mortaldev.jbcrates.modules.profile;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.utils.GSON;

import java.io.File;
import java.util.UUID;

public class CrateProfileCRUD {

  static String mainFilePath = Main.getInstance().getDataFolder() + "/profiles/";

  public static String getMainFilePath() {
    return mainFilePath;
  }

  public static void saveProfile(CrateProfile crateProfile) {
    File filePath = new File(mainFilePath + crateProfile.getUuid() + ".json");
    GSON.saveJsonObject(filePath, crateProfile);
  }

  public static CrateProfile getProfile(UUID uuid) {
    File filePath = new File(mainFilePath + uuid + ".json");
    if (filePath.exists()) {
      return GSON.getJsonObject(filePath, CrateProfile.class);
    } else {
      return null;
    }
  }

  public static void deleteProfile(UUID uuid) {
    File filePath = new File(mainFilePath + uuid + ".json");
    if (filePath.exists()) {
      filePath.delete();
    }
  }
}
