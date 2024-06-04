package me.mortaldev.jbcrates.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    /**
     * Move the entry with the given key in the LinkedHashMap by the specified shift amount.
     *
     * @param map   the LinkedHashMap to modify
     * @param key   the key of the entry to move
     * @param shift the amount to shift the entry
     * @param <K>   the type of the keys in the map
     * @param <V>   the type of the values in the map
     * @return a new LinkedHashMap with the entry moved, or the original map if the key or shift is invalid
     */
    public static <K, V> LinkedHashMap<K, V> moveEntry(LinkedHashMap<K, V> map, K key, int shift) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getKey().equals(key)) {
                index = i;
                break;
            }
        }
        if (index > -1 && index + shift >= 0 && index + shift < list.size()) {
            Map.Entry<K, V> entry = list.remove(index);
            list.add(index + shift, entry);
        }
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        list.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    /**
     * Returns the given value clamped between the minimum and maximum values.
     *
     * @param value The value to be clamped.
     * @param min The minimum value.
     * @param max The maximum value.
     * @return The clamped value. If the value is less than the minimum value, the minimum value is returned.
     * If the value is greater than the maximum value, the maximum value is returned. Otherwise, the value itself is returned.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Determines whether the given inventory can hold the specified amount of items.
     *
     * @param inventory The inventory to check.
     * @param amountOfItems The amount of items to check if the inventory can hold.
     * @return True if the inventory can hold the specified amount of items, false otherwise.
     */
    public static boolean canInventoryHold(Inventory inventory, int amountOfItems){
        if (inventory.firstEmpty() == -1) {
            return false;
        }
        int nulls = 0;
        for (ItemStack itemStack : inventory.getStorageContents()) {
            if (itemStack == null) {
                nulls++;
            }
        }
      return nulls >= amountOfItems;
    }

    /**
     * Converts the given ItemStack into a formatted item name string.
     *
     * @param itemStack The ItemStack to convert.
     * @return The formatted item name string.
     */
    public static String itemName(ItemStack itemStack){
        String name = itemStack.getType().getKey().getKey().replaceAll("_", " ").toLowerCase();
        // "lapis lazuli"

        String[] strings = name.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string.length() > 1) {
                string = string.substring(0, 1).toUpperCase() + string.substring(1);
                if (i+1 < strings.length) {
                    stringBuilder.append(string).append(" ");
                } else {
                    stringBuilder.append(string);
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Converts the given ItemStack into a grammatically correct item name string.
     *
     * @param itemStack The ItemStack to convert.
     * @return The grammatically correct item name string.
     */
    public static String grammarItem(ItemStack itemStack){
        String name = itemName(itemStack);
        String type = String.valueOf(name.charAt(name.length() - 1));
        if (itemStack.getAmount() > 1) {
            if (type.equalsIgnoreCase("s") || type.equalsIgnoreCase("z")) {
                return itemStack.getAmount() + " " + name + "s";
            } else {
                return itemStack.getAmount() + " " + name;
            }
        } else {
            if (type.equals("s") || type.equals("z")) {
                return name;
            } else {
                type = String.valueOf(name.charAt(0));
                if (type.equalsIgnoreCase("a") || type.equalsIgnoreCase("e") || type.equalsIgnoreCase("i") || type.equalsIgnoreCase("o") || type.equalsIgnoreCase("u")) {
                    return "an " + name;
                } else {
                    return "a " + name;
                }
            }
        }
    }

}
