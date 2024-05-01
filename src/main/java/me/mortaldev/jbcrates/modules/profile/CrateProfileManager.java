package me.mortaldev.jbcrates.modules.profile;

import java.io.File;
import java.util.*;

public class CrateProfileManager {

  static Map<UUID, CrateProfile> crateProfileList;
  static List<UUID> crateActiveList = new ArrayList<>();
  static List<UUID> wasOfflineList = new ArrayList<>();

  public static List<UUID> getWasOfflineList() {
    return wasOfflineList;
  }
  public static void clearWasOfflineList() {
    wasOfflineList.clear();
  }
  public static void addToWasOfflineList(UUID uuid){
    wasOfflineList.add(uuid);
  }
  public static void removeFromWasOfflineList(UUID uuid){
    wasOfflineList.remove(uuid);
  }

  public static List<UUID> getCrateActiveList() {
    return crateActiveList;
  }
  public static void clearCrateActiveList() {
    crateActiveList.clear();
  }
  public static void addToCrateActiveList(UUID uuid){
    crateActiveList.add(uuid);
  }
  public static void removeFromCrateActiveList(UUID uuid){
    crateActiveList.remove(uuid);
  }

  public static void loadCrateProfileMap() {
    crateProfileList = new HashMap<>();
    File mainPath = new File(CrateProfileCRUD.getMainFilePath());
    if (!mainPath.exists()) {
      return;
    }
    File[] files = mainPath.listFiles();
    if (files != null) {
      for (File file : files) {
        UUID uuid = UUID.fromString(file.getName().replace(".json", ""));
        CrateProfile crateProfile = CrateProfileCRUD.getProfile(uuid);
        crateProfileList.put(uuid, crateProfile);
        CrateProfileCRUD.deleteProfile(uuid);
      }
    }
  }

  public static void saveCrateProfileMap() {
    if (crateProfileList != null) {
      for (Map.Entry<UUID, CrateProfile> entry : crateProfileList.entrySet()) {
        CrateProfileCRUD.saveProfile(entry.getValue());
      }
    }
  }

  public static CrateProfile getCrateProfile(UUID uuid){
    return crateProfileList.get(uuid);
  }
  public static boolean hasCrateProfile(UUID uuid){
    return crateProfileList.containsKey(uuid);
  }

  public static void addCrateProfile(CrateProfile crateProfile) {
    crateProfileList.put(crateProfile.getUuid(), crateProfile);
  }
  public static void removeCrateProfile(UUID uuid){
    crateProfileList.remove(uuid);
  }
  public static void removeCrateProfile(CrateProfile crateProfile){
    crateProfileList.remove(crateProfile.getUuid());
  }
  public static void updateCrateProfile(CrateProfile crateProfile){
    removeCrateProfile(crateProfile);
    addCrateProfile(crateProfile);
  }
}
