package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.jbcrates.modules.menu.MenuItem;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrateManager {
    static List<Crate> crateList;

    public static void updateCratesList() {
        crateList = new ArrayList<>();
        File mainPath = new File(CrateCRUD.getMainFilePath());
        if (!mainPath.exists()) {
            return;
        }
        for (File file : mainPath.listFiles()) {
            Crate crate = CrateCRUD.getCrate(file.getName().replace(".json", ""));
            crateList.add(crate);
        }
    }

    public static List<Crate> getCrates() {
        if (crateList == null) {
            updateCratesList();
        }
        return crateList;
    }

    public static String stringToIDFormat(String string) {
        string = TextUtil.removeDecoration(string);
        string = TextUtil.removeColors(string);
        return string.replaceAll(" ", "_").toLowerCase();
    }

    public static boolean crateByIDExists(String id) {
        String newID = stringToIDFormat(id);
        for (Crate crate : crateList) {
            if (crate.getId().equals(newID)) {
                return true;
            }
        }
        return false;
    }

    public static int crateByIDCount(String id) {
        String newID = stringToIDFormat(id);
        int count = 0;
        for (Crate crate : crateList) {
            if (crate.getId().replaceAll("_\\d", "").equals(newID)) {
                count++;
            }
        }
        return count;
    }

    public static Crate getCrate(String id){
        for (Crate crate : crateList) {
            if (crate.getId().equals(id)) {
                return crate;
            }
        }
        return null;
    }

    public static void addCrate(Crate crate) {
        crateList.add(crate);
        CrateCRUD.saveCrate(crate);
    }

    public static void removeCrate(Crate crate) {
        crateList.remove(crate);
        CrateCRUD.deleteCrate(crate.id);
    }

    public static void updateCrate(String crateID, Crate newCrate) {
        crateList.remove(getCrate(crateID));
        crateList.add(newCrate);
        CrateCRUD.saveCrate(newCrate);
    }

    public static List<Component> getCrateRewardsText(Crate crate, int maxItemsListed) {
        List<Component> rewardsText = new ArrayList<>();
        if (crate.getRewardsMap().isEmpty() || crate.getRewardsMap() == null) {
            rewardsText.add(TextUtil.format("&cNo Rewards Inside."));
        } else {
            int i = 0;
            for (Map.Entry<ItemStack, Float> entry : crate.getRewardsMap().entrySet()) {
                if (i <= maxItemsListed) {
                    i++;
                    ItemStack itemStack = entry.getKey();
                    Float chance = entry.getValue();
                    String displayName = "&f" + Utils.itemName(itemStack);
                    if (itemStack.getItemMeta().hasDisplayName()) {
                        if (itemStack.displayName() instanceof TextComponent component) {
                            displayName = component.content();
                        }
                    }
                    rewardsText.add(
                            TextUtil.format(
                                    "&f" + displayName + " x" + itemStack.getAmount() + " " + chance + "%"));
                } else {
                    break;
                }
            }
        }
        return rewardsText;
    }


    public static MenuItem crateMenuItem(Crate crate) {
        MenuItem.Builder builder =
                MenuItem.builder(Material.CHEST)
                        .name(crate.getDisplayName())
                        .addLore("&7" + crate.getId())
                        .addLore("&7" + crate.getDescription())
                        .addLore("&7");

        List<Component> crateRewardsText = CrateManager.getCrateRewardsText(crate, 7);
        return builder.addLore(crateRewardsText).addLore("&7").addLore("&e[EDIT]").build();
    }
}
