package me.mortaldev.jbcrates.menus;

import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.entity.Player;

public class CratesMenu extends Gui {
    private static final int LINES = 3;
    private static final String TITLE = "&3&lCrates";

    public CratesMenu(Player player) {
        super(player, LINES, TITLE);
    }

    // Notes to remove later:
    // Going to try using lucko.helper api for GUIs / Menus
    // https://github.com/lucko/helper
    //
    // Was busy planning out the guis in the game first.
    @Override
    public void redraw() {
        new MenuScheme().scheme();
    }
}
