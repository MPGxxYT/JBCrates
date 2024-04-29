package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.Menu;
import me.mortaldev.jbcrates.modules.menu.MenuItem;
import me.mortaldev.jbcrates.utils.ItemStackBuilder;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewCratesMenu extends Menu {
    private static final MenuItem WHITE_GLASS =
            MenuItem.builder(Material.WHITE_STAINED_GLASS_PANE).name("&7").build();
    private static final MenuItem SEARCH_BUTTON =
            MenuItem.builder(Material.ANVIL)
                    .name("&f&lSearch")
                    .addLore("&7Click to search through crates.")
                    .slot(22)
                    .build();
    private static final MenuItem ADD_BUTTON =
            MenuItem.builder(Material.LIME_DYE)
                    .name("&2&lAdd Crate")
                    .addLore("&7Click to create & add a new crate.")
                    .slot(23)
                    .build();
    private static final MenuItem BACK_BUTTON =
            MenuItem.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to previous page")
                    .slot(18)
                    .build();
    private static final MenuItem NEXT_BUTTON =
            MenuItem.builder(Material.ARROW)
                    .name("&2&lNext")
                    .addLore("&7Click to view next page")
                    .slot(26)
                    .build();
    private static final List<Crate> crateList = CrateManager.getCrates();
    int currentPage = 1;

    int getMaxPage() {
        return (int) Math.ceil((double) crateList.size() / 45);
    }

    public ViewCratesMenu() {
        super("&3&lView Crates", Utils.clamp((int) Math.ceil((double) crateList.size() / 9), 3, 6));
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
        if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), ADD_BUTTON)) {
            new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Add Crate")
                    .itemLeft(ItemStackBuilder.builder(Material.CHEST).name("&7Crate Name").build())
                    .onClick(
                            (slot, stateSnapshot) -> {
                                if (slot == 2) {
                                    String id = stateSnapshot.getText();
                                    if (CrateManager.crateByIDExists(id)) {
                                        id = id + "_" + CrateManager.crateByIDCount(id);
                                    }
                                    Crate crate = new Crate(id);
                                    CrateManager.addCrate(crate);
                                    Inventory inventory =
                                            new ManageCrateMenu(crate.getId()).createInventory(stateSnapshot.getPlayer());
                                    return List.of(AnvilGUI.ResponseAction.openInventory(inventory));
                                }
                                return Collections.emptyList();
                            })
                    .open(player);
        } else if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), BACK_BUTTON)) {
            if (currentPage == 1) {
                player.openInventory(new InitialCratesMenu().createInventory(player));
            } else {
                currentPage--;
            }
        } else if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), NEXT_BUTTON)) {
            if (currentPage < getMaxPage()) {
                currentPage++;
            }
        } else if (e.getCurrentItem().getType() == Material.CHEST
                && e.getCurrentItem().lore() != null
                && e.getCurrentItem().lore().size() > 5) {
            List<Component> loreList = e.getCurrentItem().lore();
            if (loreList.get(loreList.size() - 1).equals(TextUtil.format("&e[EDIT]"))) {
                String crateID =
                        loreList.get(0) instanceof TextComponent
                                ? ((TextComponent) loreList.get(0)).content()
                                : null;
                player.openInventory(new ManageCrateMenu(crateID).createInventory(player));
            }
        }
    }


    @Override
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (crateList.isEmpty()) {
            return;
        }
        for (int i = 0, j = (currentPage - 1) * 45;
             i < Utils.clamp(crateList.size(), 1, 45);
             i++, j++) {
            Crate crate = crateList.get(j);
            ItemStack itemStack = CrateManager.crateMenuItem(crate).getItemStack();
            e.getInventory().setItem(i, itemStack);
        }
    }

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
                if (getMaxPage() > 1) {
                    add(NEXT_BUTTON);
                } else {
                    add(WHITE_GLASS.clone().setSlot(NEXT_BUTTON.getSlot()));
                }
            }
        };
    }
}
