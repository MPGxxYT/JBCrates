package me.mortaldev.jbcrates.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import org.bukkit.inventory.ItemStack;

public class ItemStackDeserializer extends JsonDeserializer<ItemStack> {
  @Override
  public ItemStack deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    String serialized = jsonParser.getValueAsString();
    if (serialized == null || serialized.isEmpty()) {
      Main.log("Failed to deserialize ItemStack!");
      return null;
    }
    return ItemStackHelper.deserialize(serialized);
  }
}
