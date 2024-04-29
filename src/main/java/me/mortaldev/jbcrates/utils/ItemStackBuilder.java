package me.mortaldev.jbcrates.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackBuilder {

    ItemStack itemStack;

    public ItemStackBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStackBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public static ItemStackBuilder builder(ItemStack itemStack){
        return new ItemStackBuilder(itemStack);
    }

    public static ItemStackBuilder builder(Material material){
        return new ItemStackBuilder(material);
    }

    public ItemStackBuilder lore(List<Component> lore){
        ItemStack cloneItemStack = itemStack.clone();
        cloneItemStack.editMeta(itemMeta -> itemMeta.lore(lore));

        this.itemStack = cloneItemStack;
        return this;
    }

    public ItemStackBuilder addLore(String lore){
        return addLore(TextUtil.format(lore));
    }

    public ItemStackBuilder addLore(Iterable<Component> components){
        for (Component component : components) {
            addLore(component);
        }
        return this;
    }

    public ItemStackBuilder addLore(Component lore){
        List<Component> loreList = this.itemStack.lore();
        if (loreList == null) {
            loreList = new ArrayList<>();
        }
        loreList.add(lore);
        this.itemStack.lore(loreList);
        return this;
    }

    public ItemStackBuilder name(String name){
        return name(TextUtil.format(name));
    }

    public ItemStackBuilder name(Component name){
        this.itemStack.editMeta(itemMeta -> itemMeta.displayName(name));
        return this;
    }

    public ItemStack build(){
        return itemStack;
    }
}
