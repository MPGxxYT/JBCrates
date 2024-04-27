package me.mortaldev.jbcrates.modules.menu;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * The Menu class represents an abstract menu that can be displayed in the game.
 * It implements the Listener interface to handle inventory events.
 */
public abstract class Menu implements Listener {
    Inventory inventory;
    String name;
    int slotRows;
    ItemStack filler;
    static Map<String, Boolean> registeredMenus = new HashMap<>();
    static Map<UUID, Inventory> registeredInventories = new HashMap<>();

     void registered() {
         registeredMenus.put(name, true);
    }

    private void register() {
        if (!registeredMenus.containsKey(name)) {
            Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());

            registered();
        }
    }


    public Menu(String name, int slotRows, ItemStack filler) {
        this.name = name;
        this.slotRows = slotRows;
        this.filler = filler;
        register();
    }

    public Menu(String name, int slotRows, Material filler) {
        this(name, slotRows, new ItemStack(filler));
    }

    public Menu(String name, int slotRows) {
        this(name, slotRows, new ItemStack(Material.AIR));
    }

    public Inventory createInventory(Player player) {
        slotRows = Utils.clamp(slotRows, 1, 6);
        Inventory gui = Bukkit.createInventory(player, slotRows * 9, TextUtil.format(name));
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }
        for (MenuItem i : getMenuItems()) {
            gui.setItem(i.getSlot(), i.getItemStack());
        }
        inventory = gui;
        registeredInventories.put(player.getUniqueId(), gui);
        return gui;
    }

    public abstract void onInventoryClick(InventoryClickEvent e);

    public abstract void onInventoryOpen(InventoryOpenEvent e);
    public abstract void onInventoryClose(InventoryCloseEvent e);

    /**
     * Retrieves a collection of MenuItems that represent the items in the menu.
     *
     * @return An Iterable collection of MenuItems representing the menu items.
     */
    public abstract Iterable<MenuItem> getMenuItems();

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Determines if the specified item at the given slot is the clicked menu item.
     *
     * @param slot The slot index of the item in the inventory.
     * @param item The ItemStack of the item in the inventory.
     * @param menuItem The MenuItem object representing the clicked menu item.
     * @return {@code true} if the item at the specified slot is the clicked menu item, {@code false} otherwise.
     */
    public boolean isClickedMenuItem(int slot, ItemStack item, MenuItem menuItem){
        return (item != null && item.getType().equals(menuItem.getItemStack().getType()) && slot == menuItem.getSlot());
    }

    public Player humanEntityToPlayer(HumanEntity humanEntity){
        return humanEntity instanceof Player ? ((Player) humanEntity) : null;
    }

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent e) {
        Inventory playerInventory = registeredInventories.get(e.getPlayer().getUniqueId());
        if (e.getInventory().equals(playerInventory)) {
            onInventoryOpen(e);
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        Inventory playerInventory = registeredInventories.get(e.getWhoClicked().getUniqueId());
        if (e.getInventory().equals(playerInventory)) {
            onInventoryClick(e);
        }
    }
    @EventHandler
    public void inventoryClose(InventoryCloseEvent e) {
        Inventory playerInventory = registeredInventories.get(e.getPlayer().getUniqueId());
        if (e.getInventory().equals(playerInventory)) {
            registeredInventories.remove(e.getPlayer().getUniqueId());
            onInventoryClose(e);
        }
    }
}