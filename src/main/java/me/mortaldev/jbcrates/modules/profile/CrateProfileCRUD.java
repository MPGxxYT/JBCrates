package me.mortaldev.jbcrates.modules.profile;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.serializers.ItemStackDeserializer;
import me.mortaldev.jbcrates.serializers.ItemStackSerializer;
import org.bukkit.inventory.ItemStack;

public class CrateProfileCRUD extends CRUD<CrateProfile> {

  private static final String PATH = Main.getInstance().getDataFolder() + "/profiles/";

  private static class Singleton {
    private static final CrateProfileCRUD INSTANCE = new CrateProfileCRUD();
  }

  public static CrateProfileCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private CrateProfileCRUD() {
    super(Jackson.getInstance());
  }

  @Override
  public Class<CrateProfile> getClazz() {
    return CrateProfile.class;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    return new CRUDAdapters()
        .addSerializer(ItemStack.class, new ItemStackSerializer())
        .addDeserializer(ItemStack.class, new ItemStackDeserializer());
  }

  @Override
  public String getPath() {
    return PATH;
  }
}
