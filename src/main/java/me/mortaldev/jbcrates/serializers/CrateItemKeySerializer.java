package me.mortaldev.jbcrates.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import me.mortaldev.jbcrates.modules.crate.CrateItem;
import org.bukkit.inventory.ItemStack;

public class CrateItemKeySerializer extends JsonSerializer<CrateItem> {

  @Override
  public void serialize(
      CrateItem crateItem, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (crateItem == null) {
      jsonGenerator.writeFieldName(""); // Write an empty key if type is wrong
      return;
    }

    ItemStack itemStack = crateItem.getItemStack();
    String displayText = crateItem.getDisplayText();

    // Ensure we handle nulls gracefully
    if (itemStack == null || displayText == null) {
      jsonGenerator.writeFieldName("");
      return;
    }

    // Encode both parts to Base64 to ensure no illegal characters
    String itemStackB64 = Base64.getEncoder().encodeToString(itemStack.serializeAsBytes());
    String displayTextB64 =
        Base64.getEncoder().encodeToString(displayText.getBytes(StandardCharsets.UTF_8));

    // Combine them with a unique, safe separator
    String key = itemStackB64 + ":::" + displayTextB64;
    jsonGenerator.writeFieldName(key);
  }
}
