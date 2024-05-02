package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.utils.GSON;
import org.bukkit.Bukkit;

import java.io.File;

public class CrateCRUD {

    static String mainFilePath = Main.getInstance().getDataFolder() + "/crates/";

    public static String getMainFilePath() {
        return mainFilePath;
    }

    /**
     * Saves a Crate object to a file in JSON format and returns the saved object.
     *
     * @param crateObject the Crate object to be saved
     * @return the saved Crate object
     */
    public static Crate saveCrate(Crate crateObject) {
        File filePath = new File(mainFilePath + crateObject.id + ".json");
        GSON.saveJsonObject(filePath, crateObject);
        //Bukkit.getLogger().info("Crate '" + crateObject.getId() + "' has been saved.");
        return crateObject;
    }

    /**
     * Retrieves a Crate object based on the given crateID.
     *
     * @param crateID the ID of the crate to retrieve
     * @return the Crate object with the specified crateID
     * @throws IllegalArgumentException if the crate with the specified crateID does not exist
     */
    public static Crate getCrate(String crateID) {
        File filePath = new File(mainFilePath + crateID + ".json");
        if (filePath.exists()) {
            return GSON.getJsonObject(filePath, Crate.class);
        } else {
            throw new IllegalArgumentException("Could not get crate: '" + crateID + "' does not exist.");
        }
    }

    /**
     * Deletes a crate file based on the provided crate ID.
     *
     * @param crateID the ID of the crate to be deleted
     * @throws IllegalArgumentException if the crate with the specified crateID does not exist
     */
    public static void deleteCrate(String crateID) {
        File filePath = new File(mainFilePath + crateID + ".json");
        if (filePath.exists()) {
            filePath.delete();
            Bukkit.getLogger().info("Crate '" + crateID + "' has been deleted.");
        } else {
            throw new IllegalArgumentException("Could not delete crate: '" + crateID + "' does not exist.");
        }
    }
}
