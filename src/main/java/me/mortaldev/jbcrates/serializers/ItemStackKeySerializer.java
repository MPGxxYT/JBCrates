package me.mortaldev.jbcrates.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;

public class ItemStackKeySerializer extends JsonSerializer<ItemStack> {

  @Override
  public void serialize(ItemStack value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    if (value == null) {
      return;
    }
    byte[] itemStackBytes = value.serializeAsBytes();
    String base64String = Base64.getEncoder().encodeToString(itemStackBytes);

    gen.writeFieldName(base64String);
  }
}
