package me.mortaldev.jbcrates.utils;

import me.mortaldev.jbcrates.Main;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.UUID;

public class Cooldown {
    HashMap<UUID, Long> mapLength; // A map of the users, and their cooldown length.
    HashMap<UUID, Long> mapUsers; // A map of the users on cooldown, with their timestamp

    public Cooldown() {
        this.mapUsers = new HashMap<>();
        this.mapLength = new HashMap<>();
    }

    /**
     * Will begin the cooldown once called.
     * @param uuid Player's uuid
     * @param length Length in millis of the cooldown
     */

    // 50ms is 1tick
    public void start(UUID uuid, Long length){
        long ticks = (long) Math.ceil((double) length / 50);
        mapUsers.put(uuid, System.currentTimeMillis());
        mapLength.put(uuid, length);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> end(uuid), ticks);
    }

    public void end(UUID uuid){
        mapUsers.remove(uuid);
        mapLength.remove(uuid);
    }

    public boolean isDone(UUID uuid){
        if (!mapLength.containsKey(uuid)){
            return true;
        }
        long length = mapLength.get(uuid);
        long timeElapsed = System.currentTimeMillis() - mapUsers.get(uuid);
        if (timeElapsed >= length) {
            end(uuid);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param uuid The player's uuid.
     * @return Time left in seconds as an int.
     */
    public int getTimeLeft(UUID uuid){
        long length = mapLength.get(uuid);
        long timeElapsed = System.currentTimeMillis() - mapUsers.get(uuid);
        return Math.round((float) (length - timeElapsed)/1000);
    }
}