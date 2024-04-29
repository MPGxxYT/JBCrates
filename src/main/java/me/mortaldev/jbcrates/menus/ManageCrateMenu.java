package me.mortaldev.jbcrates.menus;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.Menu;
import me.mortaldev.jbcrates.modules.menu.MenuItem;
import me.mortaldev.jbcrates.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class ManageCrateMenu extends Menu {

    private static Crate activeCrate;
    private static MenuItem nameButton;
    private static MenuItem descriptionButton;
    private static MenuItem rewardsButton;
    private static MenuItem deleteButton;
    private static final MenuItem BACK_BUTTON =
            MenuItem.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to previous page")
                    .slot(18)
                    .build();

    public ManageCrateMenu(String crateID) {
        super("&3&lEditing Crate: &f" + crateID, 3);
        activeCrate = CrateManager.getCrate(crateID);
        setupButtons();
    }

    void setupButtons() {
        nameButton =
                MenuItem.builder(Material.NAME_TAG)
                        .slot(10)
                        .name("&3&lNAME:")
                        .addLore("&7")
                        .addLore(activeCrate.getDisplayName())
                        .addLore("&7")
                        .addLore("&e[RENAME]")
                        .build();
        descriptionButton =
                MenuItem.builder(Material.PAPER)
                        .slot(12)
                        .name("&3&lDESCRIPTION:")
                        .addLore("&7")
                        .addLore(activeCrate.getDescription())
                        .addLore("&7")
                        .addLore("&e[CHANGE]")
                        .build();

        List<Component> crateRewardsText = CrateManager.getCrateRewardsText(activeCrate, 7);
        rewardsButton =
                MenuItem.builder(Material.CHEST)
                        .slot(14)
                        .name("&3&lREWARDS:")
                        .addLore("&7")
                        .addLore(crateRewardsText)
                        .addLore("&7")
                        .addLore("&e[EDIT]")
                        .build();
        deleteButton =
                MenuItem.builder(Material.RED_DYE)
                        .slot(16)
                        .name("&c&lDELETE:")
                        .addLore("&7")
                        .addLore("&cDelete crate &l" + activeCrate.getId() + "&c?")
                        .addLore("&7")
                        .build();
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
        if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), BACK_BUTTON)) {
            player.openInventory(new ViewCratesMenu().createInventory(player));
        } else if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), nameButton)) {
            new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Rename Crate: " + activeCrate.getId())
                    .itemLeft(
                            ItemStackBuilder.builder(Material.NAME_TAG)
                                    .name(activeCrate.getDisplayName())
                                    .build())
                    .onClick(
                            (slot, stateSnapshot) -> {
                                if (slot == 2) {
                                    activeCrate.setDisplayName(stateSnapshot.getText());
                                    CrateManager.updateCrate(activeCrate.getId(), activeCrate);
                                }
                                Inventory inventory =
                                        new ManageCrateMenu(activeCrate.getId())
                                                .createInventory(stateSnapshot.getPlayer());
                                return List.of(AnvilGUI.ResponseAction.openInventory(inventory));
                            })
                    .open(player);
        } else if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), descriptionButton)) {
            new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Crate Desc: " + activeCrate.getId())
                    .itemLeft(
                            ItemStackBuilder.builder(Material.PAPER)
                                    .name(activeCrate.getDescription())
                                    .build())
                    .onClick(
                            (slot, stateSnapshot) -> {
                                if (slot == 2) {
                                    activeCrate.setDescription(stateSnapshot.getText());
                                    CrateManager.updateCrate(activeCrate.getId(), activeCrate);
                                }
                                Inventory inventory =
                                        new ManageCrateMenu(activeCrate.getId())
                                                .createInventory(stateSnapshot.getPlayer());
                                return List.of(AnvilGUI.ResponseAction.openInventory(inventory));
                            })
                    .open(player);

        } else if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), rewardsButton)) {

        } else if (isClickedMenuItem(e.getSlot(), e.getCurrentItem(), deleteButton)) {
            player.openInventory(new ConfirmDeleteCrateMenu(activeCrate.getId()).createInventory(player));
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
                add(nameButton);
                add(descriptionButton);
                add(rewardsButton);
                add(deleteButton);
                add(BACK_BUTTON);
            }
        };
    }
}
