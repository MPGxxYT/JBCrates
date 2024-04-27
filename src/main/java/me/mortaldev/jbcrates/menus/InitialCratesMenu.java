package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.modules.menu.Menu;
import me.mortaldev.jbcrates.modules.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.ArrayList;

public class InitialCratesMenu extends Menu {

    private static final MenuItem CHEST_MENU_ITEM = MenuItem.builder(Material.CHEST).slot(13).name("&3&lView Crates")
            .addLore("&7Click to view crates")
            .build();

    public InitialCratesMenu() {
        super("&3&lJBCrates", 3);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = humanEntityToPlayer(e.getWhoClicked());
        if (player == null) {
            return;
        }
        e.setCancelled(true);
        if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), CHEST_MENU_ITEM)){
            player.openInventory(new ViewCratesMenu().createInventory(player));
        }
    }

    @Override
    public void onInventoryOpen(InventoryOpenEvent e) {}

    @Override
    public void onInventoryClose(InventoryCloseEvent e) {}

    @Override
    public Iterable<MenuItem> getMenuItems() {
        return new ArrayList<>(){{
            add(CHEST_MENU_ITEM);
        }};
    }
}
