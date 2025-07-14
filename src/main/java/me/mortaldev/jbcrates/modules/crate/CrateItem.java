package me.mortaldev.jbcrates.modules.crate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.TextUtil;
import org.bukkit.inventory.ItemStack;

public class CrateItem implements Cloneable {

  private ItemStack itemStack;
  private String displayText;

  /**
   * Constructor for Jackson (JSON serialization/deserialization). It allows creating a CrateItem
   * from saved data.
   */
  @JsonCreator
  public CrateItem(
      @JsonProperty("originalItem") ItemStack originalItem,
      @JsonProperty("displayText") String displayText) {
    this.itemStack = originalItem;
    this.displayText = displayText;
  }

  /**
   * Convenience constructor for creating a new CrateItem in code. It's ignored by Jackson to
   * prevent conflicts with the creator constructor.
   */
  @JsonIgnore
  public CrateItem(ItemStack originalItem) {
    this.itemStack = originalItem;
    // Use the helper to get the item's name as the default display text
    this.displayText = ItemStackHelper.getName(originalItem).asString();
  }

  public String getDisplayText() {
    return displayText;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }

  public void setItemStack(ItemStack itemStack) {
    this.itemStack = itemStack;
  }

  @JsonIgnore
  public String getPlainDisplay() {
    return TextUtil.removeDecorAndColor(displayText);
  }

  public void setDisplayText(String displayText) {
    this.displayText = displayText;
  }

  /**
   * equals() and hashCode() are essential for this object to work correctly as a key in a Map (like
   * the one inside ChanceMap). They ensure that two CrateItem objects are considered "equal" if
   * their underlying ItemStacks are the same.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CrateItem crateItem = (CrateItem) o;
    // ItemStack.equals() is a strict comparison. If you want to ignore stack size,
    // you might consider using isSimilar() instead, but equals() is safer for map keys.
    return Objects.equals(itemStack, crateItem.itemStack);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemStack);
  }

  @Override
  public CrateItem clone() {
    try {
      CrateItem cloned = (CrateItem) super.clone();
      // Deep copy the ItemStack to prevent shared state issues
      if (this.itemStack != null) {
        cloned.itemStack = this.itemStack.clone();
      }
      // The displayText is a String, which is immutable, so a shallow copy is fine.
      cloned.displayText = this.displayText;
      return cloned;
    } catch (CloneNotSupportedException e) {
      // This should not happen since we implement Cloneable
      throw new AssertionError(e);
    }
  }
}
