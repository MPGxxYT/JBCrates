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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Menu implements Listener {
    Inventory inventory;
    InventoryType inventoryType;
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

    public Menu(String name, int slotRows, ItemStack filler, InventoryType inventoryType) {
        this.name = name;
        this.slotRows = slotRows;
        this.filler = filler;
        this.inventoryType = inventoryType;
        register();
    }

    public Menu(String name, int slotRows, Material filler) {
        this(name, slotRows, new ItemStack(filler), InventoryType.CHEST);
    }

    public Menu(String name, int slotRows) {
        this(name, slotRows, new ItemStack(Material.AIR), InventoryType.CHEST);
    }

    public Menu(String name) {
        this(name, 1, new ItemStack(Material.AIR), InventoryType.CHEST);
    }

    public Menu(String name, int slotRows, Material filler, InventoryType inventoryType) {
        this(name, slotRows, new ItemStack(filler), inventoryType);
    }

    public Menu(String name, int slotRows, InventoryType inventoryType) {
        this(name, slotRows, new ItemStack(Material.AIR), inventoryType);
    }

    public Menu(String name, InventoryType inventoryType) {
        this(name, 1, new ItemStack(Material.AIR), inventoryType);
    }

    public Inventory createInventory(Player player) {
        Inventory gui;
        if (inventoryType == InventoryType.CHEST) {
            slotRows = Utils.clamp(slotRows, 1, 6);
            gui = Bukkit.createInventory(player, slotRows * 9, TextUtil.format(name));
            if (filler.getType() != Material.AIR){
                for (int i = 0; i < gui.getSize(); i++) {
                    gui.setItem(i, filler);
                }
            }
        } else {
            gui = Bukkit.createInventory(player, inventoryType, TextUtil.format(name));
        }
        if (getMenuItems() != null) {
            for (MenuItem i : getMenuItems()) {
                gui.setItem(i.getSlot(), i.getItemStack());
            }
        }
        inventory = gui;
        registeredInventories.put(player.getUniqueId(), gui);
        return gui;
    }

    public void redraw(Player player) {
        player.openInventory(createInventory(player));
    }

    /**
     * Determines if the specified item at the given slot is the clicked menu item.
     *
     * @param slot The slot index of the item in the inventory.
     * @param item The ItemStack of the item in the inventory.
     * @param menuItem The MenuItem object representing the clicked menu item.
     * @return {@code true} if the item at the specified slot is the clicked menu item, {@code false}
     *     otherwise.
     */
    public boolean isClickedMenuItem(int slot, ItemStack item, MenuItem menuItem) {
        return (item != null
                && item.getType().equals(menuItem.getItemStack().getType())
                && slot == menuItem.getSlot()
                && item.displayName().equals(menuItem.getItemStack().displayName()));
    }
    public boolean isClickedMenuItem(int slot, ItemStack item, MenuItem menuItem, int... slots) {
        boolean slotsContains = false;
        for (int i : slots) {
            if (i == slot) {
                slotsContains = true;
                break;
            }
        }
        return (slotsContains && item.getType().equals(menuItem.getItemStack().getType())
                && item.displayName().equals(menuItem.getItemStack().displayName()));
    }

    public Player humanEntityToPlayer(HumanEntity humanEntity) {
        return humanEntity instanceof Player ? ((Player) humanEntity) : null;
    }

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent e) {
        Inventory playerInventory = registeredInventories.get(e.getPlayer().getUniqueId());
        if (e.getInventory().equals(playerInventory)
                && e.getView().title().equals(TextUtil.format(name))
                && e.getInventory().getType().equals(playerInventory.getType())) {
            onInventoryOpen(e);
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        Inventory playerInventory = registeredInventories.get(e.getWhoClicked().getUniqueId());
        if (e.getInventory().equals(playerInventory)
                && e.getView().title().equals(TextUtil.format(name))
                && e.getInventory().getType().equals(playerInventory.getType())) {
            onInventoryClick(e);
        }
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent e) {
        Inventory playerInventory = registeredInventories.get(e.getPlayer().getUniqueId());
        if (e.getInventory().equals(playerInventory)
                && e.getView().title().equals(TextUtil.format(name))
                && e.getInventory().getType().equals(playerInventory.getType())) {
            registeredInventories.remove(e.getPlayer().getUniqueId());
            onInventoryClose(e);
        }
    }

    public abstract void onInventoryClick(InventoryClickEvent e);
    public abstract void onInventoryOpen(InventoryOpenEvent e);
    public abstract void onInventoryClose(InventoryCloseEvent e);
    public abstract Iterable<MenuItem> getMenuItems();
}
