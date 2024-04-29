package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.Menu;
import me.mortaldev.jbcrates.modules.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.ArrayList;

public class ConfirmDeleteCrateMenu extends Menu {

    private static MenuItem confirmButton;
    private static MenuItem cancelButton;
    private static MenuItem crateIcon;
    private static String crateID;

    public ConfirmDeleteCrateMenu(String crateID) {
        super("&c&lConfirm Delete: " + crateID, 3);
        ConfirmDeleteCrateMenu.crateID = crateID;
        setupItems();
    }

    void setupItems() {
        confirmButton =
                MenuItem.builder(Material.LIME_STAINED_GLASS_PANE)
                        .name("&2&lYES! DELETE " + crateID)
                        .build();
        cancelButton =
                MenuItem.builder(Material.RED_STAINED_GLASS_PANE)
                        .name("&c&lNO! DONT DELETE " + crateID)
                        .build();
        Crate crate = CrateManager.getCrate(crateID);
        crateIcon = CrateManager.crateMenuItem(crate).setSlot(13);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = humanEntityToPlayer(e.getWhoClicked());
        if (player == null) {
            return;
        }
        e.setCancelled(true);
        if (e.getCurrentItem() == null) {
            return;
        }
        if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), cancelButton, 9, 10, 11)) {
            player.openInventory(new ManageCrateMenu(crateID).createInventory(player));
        } else if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), confirmButton, 15, 16, 17)) {
            CrateManager.removeCrate(CrateManager.getCrate(crateID));
            player.openInventory(new ViewCratesMenu().createInventory(player));
        }
    }

    @Override
    public void onInventoryOpen(InventoryOpenEvent e) {}

    @Override
    public void onInventoryClose(InventoryCloseEvent e) {}

    @Override
    public Iterable<MenuItem> getMenuItems() {
        return new ArrayList<>() {
            {
                add(cancelButton.clone().setSlot(9));
                add(cancelButton.clone().setSlot(10));
                add(cancelButton.clone().setSlot(11));
                add(crateIcon);
                add(confirmButton.clone().setSlot(15));
                add(confirmButton.clone().setSlot(16));
                add(confirmButton.clone().setSlot(17));
            }
        };
    }
}
