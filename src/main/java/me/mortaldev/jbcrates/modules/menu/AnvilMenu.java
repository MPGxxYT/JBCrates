package me.mortaldev.jbcrates.modules.menu;

import me.mortaldev.jbcrates.Main;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class AnvilMenu {
    AnvilGUI.Builder builder;
    public AnvilMenu(String name) {
        builder = new AnvilGUI.Builder().title(name);
    }

    public void openInventory(Player player){
        builder.onClose(this::onClose)
                .onClick(this::onClick)
                .itemLeft(leftItem())
                .itemRight(rightItem())
                .plugin(Main.getInstance())
                .open(player);
    }

    public AnvilGUI.Builder getBuilder() {
        return builder;
    }

    public void setBuilder(AnvilGUI.Builder builder) {
        this.builder = builder;
    }

    public abstract List<AnvilGUI.ResponseAction> onClick(Integer slot, AnvilGUI.StateSnapshot stateSnapshot);
    public abstract void onClose(AnvilGUI.StateSnapshot stateSnapshot);
    public abstract ItemStack leftItem();
    public abstract ItemStack rightItem();

}
