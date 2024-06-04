package me.mortaldev.jbcrates.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ItemStackHelper {

  public static String serialize(ItemStack itemStack) {
    byte[] itemStackAsBytes = itemStack.serializeAsBytes();
    return Base64.getEncoder().encodeToString(itemStackAsBytes);
  }

  public static ItemStack deserialize(String string) {
    byte[] bytes = Base64.getDecoder().decode(string);
    return ItemStack.deserializeBytes(bytes);
  }

  public static Builder builder(ItemStack itemStack) {
    return new Builder(itemStack);
  }

  public static Builder builder(Material material) {
    return new Builder(material);
  }

  public static class Builder {

    ItemStack itemStack;

    public Builder(ItemStack itemStack) {
      this.itemStack = itemStack;
    }

    public Builder(Material material) {
      this.itemStack = new ItemStack(material);
    }

    public Builder lore(List<Component> lore) {
      ItemStack cloneItemStack = itemStack.clone();
      cloneItemStack.editMeta(itemMeta -> itemMeta.lore(lore));

      this.itemStack = cloneItemStack;
      return this;
    }

    public Builder addLore(String lore) {
      return addLore(TextUtil.format(lore));
    }

    public Builder addLore(Iterable<Component> components) {
      for (Component component : components) {
        addLore(component);
      }
      return this;
    }

    public Builder addLore(Component lore) {
      List<Component> loreList = this.itemStack.lore();
      if (loreList == null) {
        loreList = new ArrayList<>();
      }
      loreList.add(lore);
      this.itemStack.lore(loreList);
      return this;
    }

    public Builder name(String name) {
      return name(TextUtil.format(name));
    }

    public Builder name(Component name) {
      this.itemStack.editMeta(itemMeta -> itemMeta.displayName(name));
      return this;
    }

    public ItemStack build() {
      return itemStack;
    }
  }
}
