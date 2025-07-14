package me.mortaldev.jbcrates.serializers;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import me.mortaldev.jbcrates.utils.ItemStackHelper;

public class ItemStackKeyDeserializer extends KeyDeserializer {
  @Override
  public Object deserializeKey(String s, DeserializationContext deserializationContext) {
    if (s == null) {
      return null;
    }
    return ItemStackHelper.deserialize(s);
  }
}
