package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateCRUD;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.Menu;
import me.mortaldev.jbcrates.modules.menu.MenuItem;
import me.mortaldev.jbcrates.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.ArrayList;
import java.util.List;

public class ViewCratesMenu extends Menu {
    private int currentPage = 1;
    private static final MenuItem WHITE_GLASS = MenuItem.builder(Material.WHITE_STAINED_GLASS_PANE).name("&7").build();
    private static final MenuItem SEARCH_BUTTON = MenuItem.builder(Material.ANVIL).name("&f&lSearch").addLore("&7Click to search through crates.").slot(22).build();
    private static final MenuItem ADD_BUTTON = MenuItem.builder(Material.LIME_DYE).name("&2&lAdd Crate").addLore("&7Click to create & add a new crate.").slot(23).build();
    private static final MenuItem BACK_BUTTON = MenuItem.builder(Material.ARROW).name("&c&lBack").addLore("&7Click to return to previous page").slot(18).build();
    private static final MenuItem NEXT_BUTTON = MenuItem.builder(Material.ARROW).name("&2&lNext").addLore("&7Click to view next page").slot(26).build();
    private static final List<Crate> crateList = CrateManager.getCrates();

    public ViewCratesMenu() {
        super("&3&lView Crates", Utils.clamp((int) Math.ceil((double) crateList.size() / 9), 3, 6));
        Bukkit.getLogger().info(crateList.size() +"");
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = humanEntityToPlayer(e.getWhoClicked());
        if (player == null) {
            return;
        }
        e.setCancelled(true);
        if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), ADD_BUTTON)) {
            Crate newCrate = CrateCRUD.saveCrate(new Crate("&c&lTest Crate"));
            CrateManager.addCrate(newCrate);
        }
        if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), BACK_BUTTON)) {
            if (currentPage == 1) {
                player.openInventory(new InitialCratesMenu().createInventory(player));
            }
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
                add(WHITE_GLASS.clone().setSlot(19));
                add(WHITE_GLASS.clone().setSlot(20));
                add(WHITE_GLASS.clone().setSlot(21));
                add(WHITE_GLASS.clone().setSlot(23));
                add(WHITE_GLASS.clone().setSlot(24));
                add(WHITE_GLASS.clone().setSlot(25));
                add(SEARCH_BUTTON);
                add(ADD_BUTTON);
                add(BACK_BUTTON);
                add(NEXT_BUTTON); // only gets added when there are more crates than the menu can fit
            }
        };
    }
}
