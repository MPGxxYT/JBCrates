package me.mortaldev.jbcrates.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.mortaldev.jbcrates.Main;

import java.io.*;

public class GSON {

    /**
     * Returns a {@link File} object representing the default file path based on the provided file name.
     * If the file name contains the extension ".json", the file path is constructed by concatenating
     * the data folder absolute path obtained from the main class instance and the file name.
     * If the file name does not contain the extension ".json", the file path is constructed by
     * concatenating the data folder absolute path obtained from the main class instance, the file name,
     * and the extension ".json".
     *
     * @param fileName the name of the file
     * @return a {@link File} object representing the default file path
     */
    public static File defaultFile(String fileName){
        if (fileName.contains(".json")){
            return new File(Main.getInstance().getDataFolder().getAbsolutePath() + fileName);
        } else {
            return new File(Main.getInstance().getDataFolder().getAbsolutePath() + fileName + ".json");
        }
    }

    /**
     * Retrieves a JSON object from a file and converts it to the specified class.
     *
     * @param file the file from which to retrieve the JSON object
     * @param clazz the class to which the JSON object should be converted
     * @return an instance of the specified class with the data from the JSON object, or null if the file does not exist
     * @param <T> the type of the class to which the JSON object should be converted
     */
    public static <T> T getJsonObject(File file, Class<T> clazz){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (!file.exists()){
            return null;
        }

        try (Reader reader = new FileReader(file)){
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves a JSON object to a file in a pretty-printed format.
     *
     * @param file the file to save the JSON object to
     * @param object the JSON object to be saved
     */
    public static void saveJsonObject(File file, Object object){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (Writer writer = new FileWriter(file, false)) {
                gson.toJson(object, writer);
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}