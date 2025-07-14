package me.mortaldev.jbcrates.modules.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import me.mortaldev.crudapi.CRUD;
import org.bukkit.inventory.ItemStack;

public class CrateProfile implements CRUD.Identifiable {

  private final String id;
  private final List<ItemStack> overflowRewards;

  @JsonCreator
  public CrateProfile(
      @JsonProperty("id") String id,
      @JsonProperty("overflowRewards") List<ItemStack> overflowRewards) {
    this.id = id == null ? "ID" : id;
    this.overflowRewards = overflowRewards == null ? new ArrayList<>() : overflowRewards;
  }

  public static CrateProfile create(String id) {
    return new CrateProfile(id, new ArrayList<>());
  }

  public void addItem(ItemStack itemStack) {
    overflowRewards.add(itemStack);
  }

  public void removeItem(ItemStack itemStack) {
    overflowRewards.remove(itemStack);
  }

  public List<ItemStack> getOverflowRewards() {
    return overflowRewards;
  }

  @Override
  public String getId() {
    return id;
  }
}
