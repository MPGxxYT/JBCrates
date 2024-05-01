package me.mortaldev.jbcrates.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Utils {
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
