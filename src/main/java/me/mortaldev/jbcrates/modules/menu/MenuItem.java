package me.mortaldev.jbcrates.modules.menu;

import me.mortaldev.jbcrates.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {

    ItemStack itemStack;
    Integer slot;

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Integer getSlot() {
        return slot;
    }

    public MenuItem setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public MenuItem setSlot(Integer slot) {
        this.slot = slot;
        return this;
    }

    /**
     * Creates and returns a new {@code MenuItem} object that is a copy of the current instance.
     *
     * @return a new {@code MenuItem} object that is a copy of this instance.
     */
    public MenuItem clone() {
        return new MenuItem(itemStack, slot);
    }

    public MenuItem(ItemStack itemStack, Integer slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public static MenuItem.Builder builder(ItemStack itemStack) {
        return new Builder(itemStack);
    }

    public static MenuItem.Builder builder(Material material) {
        return new Builder(material);
    }

    public static final class Builder {
        ItemStack itemStack;
        Integer slot;

        public Builder(ItemStack itemStack) {
            this.itemStack = itemStack;
            this.slot = 0;
        }

        public Builder(Material material) {
            this.itemStack = new ItemStack(material);
            this.slot = 0;
        }

        public Builder itemStack(ItemStack itemStack){
            this.itemStack = itemStack;
            return this;
        }

        public Builder lore(List<Component> lore){
            this.itemStack.lore(lore);
            return this;
        }

        public Builder addLore(String lore){
            return addLore(TextUtil.format(lore));
        }

        public Builder addLore(Component lore){
            List<Component> loreList = this.itemStack.lore();
            if (loreList == null) {
                loreList = new ArrayList<>();
            }
            loreList.add(lore);
            this.itemStack.lore(loreList);
            return this;
        }

        public Builder name(String name){
            return name(TextUtil.format(name));
        }

        public Builder name(Component name){
            this.itemStack.editMeta(itemMeta -> itemMeta.displayName(name));
            return this;
        }

        public Builder slot(int slot){
            this.slot = slot;
            return this;
        }

        public MenuItem build(){
            return new MenuItem(itemStack, slot);
        }
    }
}