package me.mortaldev.jbcrates.serializers;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import me.mortaldev.jbcrates.modules.crate.CrateItem;
import org.bukkit.inventory.ItemStack;

public class CrateItemKeyDeserializer extends KeyDeserializer {
  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    String[] parts = key.split(":::", 2);
    if (parts.length != 2) {
      throw new IOException(
          "Invalid CrateItem key format. Expected 'itemStackB64:::displayTextB64', but got: "
              + key);
    }

    try {
      byte[] itemStackBytes = Base64.getDecoder().decode(parts[0]);
      byte[] displayTextBytes = Base64.getDecoder().decode(parts[1]);

      ItemStack itemStack = ItemStack.deserializeBytes(itemStackBytes);
      String displayText = new String(displayTextBytes, StandardCharsets.UTF_8);

      // Use the constructor that takes both properties to reconstruct the object perfectly
      return new CrateItem(itemStack, displayText);
    } catch (IllegalArgumentException e) {
      throw new IOException("Failed to decode Base64 in CrateItem key: " + key, e);
    }
  }
}
