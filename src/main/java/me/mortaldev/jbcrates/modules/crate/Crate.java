package me.mortaldev.jbcrates.modules.crate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.*;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbcrates.modules.animation.Animation;
import me.mortaldev.jbcrates.utils.ChanceMap;
import me.mortaldev.jbcrates.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class Crate implements CRUD.Identifiable {

  private final String id;
  private String displayName;
  private String description = "&7This is the default description.";
  private final ChanceMap<CrateItem> rewardsMap;
  private List<CrateItem> displaySet;
  private Integer amountToWin = 1;
  private CrateManager.SortBy sortBy = CrateManager.SortBy.CHANCE;
  private CrateManager.Order order = CrateManager.Order.ASCENDING;
  private Animation animation;
  private final Long dateCreated;
  private Long lastModified;

  @JsonCreator
  public Crate(
      @JsonProperty("id") String id,
      @JsonProperty("rewardsMap") ChanceMap<CrateItem> rewardsMap,
      @JsonProperty("dateCreated") Long dateCreated) {
    this.id = id == null ? "CRATE_ID" : id;
    this.rewardsMap = rewardsMap == null ? new ChanceMap<>() : rewardsMap;
    this.dateCreated = dateCreated == null ? System.currentTimeMillis() : dateCreated;
    this.lastModified = System.currentTimeMillis();
    this.displaySet = new ArrayList<>();
    this.animation = Animation.ORBIT;
  }

  public static Crate create(String id) {
    return new Crate(id, null, null);
  }

  @Override
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public Long getDateCreated() {
    return dateCreated;
  }

  public Long getLastModified() {
    return lastModified;
  }

  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  public void modify() {
    setLastModified(System.currentTimeMillis());
  }

  public List<CrateItem> getDisplaySet() {
    return displaySet;
  }

  public void setDisplaySet(List<CrateItem> displaySet) {
    this.displaySet = displaySet;
  }

  public Animation getAnimation() {
    return animation;
  }

  public void setAnimation(Animation animation) {
    this.animation = animation;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setAmountToWin(Integer amountToWin) {
    this.amountToWin = amountToWin;
  }

  public void setSortBy(CrateManager.SortBy sortBy) {
    this.sortBy = sortBy;
  }

  public void setOrder(CrateManager.Order order) {
    this.order = order;
  }

  public String getDisplayName() {
    return displayName;
  }

  @JsonIgnore
  public String getCleanDisplayName() {
    return TextUtil.removeDecorAndColor(displayName);
  }

  public String getDescription() {
    return description;
  }

  public ChanceMap<CrateItem> getRewardsMap() {
    return rewardsMap;
  }

  public Integer getAmountToWin() {
    return amountToWin;
  }

  public CrateManager.SortBy getSortBy() {
    return sortBy;
  }

  public CrateManager.Order getOrder() {
    return order;
  }

  /**
   * Creates a new Crate instance by migrating data from the old Crate structure. This is intended
   * for a one-time data conversion process.
   *
   * @param oldCrate The instance of the old Crate class.
   * @return A new, migrated Crate object.
   */
  public static Crate fromOldCrate(Crate_Old oldCrate) {
    // 1. Create a new Crate object using its ID.
    Crate newCrate = Crate.create(oldCrate.getId());

    // 2. Copy over all the simple, direct-mapping fields.
    newCrate.setDisplayName(TextUtil.deformat(oldCrate.getDisplayName()));
    newCrate.setDescription(oldCrate.getDescription());
    newCrate.setAmountToWin(oldCrate.getAmountToWin());
    newCrate.setSortBy(oldCrate.getSortBy());
    newCrate.setOrder(oldCrate.getOrder());
    newCrate.setLastModified(System.currentTimeMillis()); // Set modification time

    // The old crate didn't have an animation, so we'll set a default.
    newCrate.setAnimation(Animation.ORBIT);

    // 3. Migrate the complex rewards structure.
    ChanceMap<CrateItem> newRewardsMap = newCrate.getRewardsMap();
    List<CrateItem> newDisplaySet = new ArrayList<>();

    // Get the old data. getRewardsDisplayMap is the source of truth for items and their names.
    LinkedHashMap<ItemStack, Component> oldRewardsWithDisplay = oldCrate.getRewardsDisplayMap();
    Map<ItemStack, Double> oldRewardsWithChance = oldCrate.getRewardsMap();

    // 4. Iterate over the old rewards to convert them.
    for (Map.Entry<ItemStack, Component> entry : oldRewardsWithDisplay.entrySet()) {
      ItemStack itemStack = entry.getKey();
      Component customDisplayName = entry.getValue();

      // Find the corresponding chance. Default to 0.0 if not found.
      Double chance = oldRewardsWithChance.getOrDefault(itemStack, 0.0);

      // Create the new CrateItem.
      CrateItem newCrateItem = new CrateItem(itemStack.clone());

      // Overwrite the default display name with the custom one from the old crate.
      newCrateItem.setDisplayText(TextUtil.deformat(customDisplayName));

      // Add the fully configured CrateItem to the new structures.
      newRewardsMap.put(
          newCrateItem, BigDecimal.valueOf(chance), false); // Add without rebalancing each time
      newDisplaySet.add(newCrateItem);
    }

    // 5. Set the populated list for the display set.
    newCrate.setDisplaySet(newDisplaySet);

    return newCrate;
  }
}
