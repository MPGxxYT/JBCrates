package me.mortaldev.jbcrates.modules.profile;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class CrateProfile {

  UUID uuid;
  List<String> itemList;

  public CrateProfile(UUID uuid) {
    this.uuid = uuid;
    this.itemList = new ArrayList<>();
  }

  String encodeItemStack(ItemStack itemStack){
    byte[] itemStackAsBytes = itemStack.serializeAsBytes();
    return Base64.getEncoder().encodeToString(itemStackAsBytes);
  }

  ItemStack decodeItemStack(String string){
    byte[] bytes = Base64.getDecoder().decode(string);
    return ItemStack.deserializeBytes(bytes);
  }

  public UUID getUuid() {
    return uuid;
  }

  public void addItem(ItemStack itemStack){
    itemList.add(encodeItemStack(itemStack));
  }

  public void removeItem(ItemStack itemStack){
    itemList.remove(encodeItemStack(itemStack));
  }

  public List<ItemStack> getItemList() {
    List<ItemStack> items = new ArrayList<>();
    for (String string : itemList) {
      items.add(decodeItemStack(string));
    }
    return items;
  }
}
