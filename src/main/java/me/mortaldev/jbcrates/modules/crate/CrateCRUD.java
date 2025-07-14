package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.serializers.*;
import org.bukkit.inventory.ItemStack;

public class CrateCRUD extends CRUD<Crate> {
  private static final String PATH = Main.getInstance().getDataFolder() + "/crates/";

  private static class Singleton {
    private static final CrateCRUD INSTANCE = new CrateCRUD();
  }

  public static CrateCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private CrateCRUD() {
    super(Jackson.getInstance());
  }

  @Override
  public Class<Crate> getClazz() {
    return Crate.class;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    return new CRUDAdapters()
        .addKeySerializer(CrateItem.class, new CrateItemKeySerializer())
        .addKeyDeserializer(CrateItem.class, new CrateItemKeyDeserializer())
        .addKeySerializer(ItemStack.class, new ItemStackKeySerializer())
        .addKeyDeserializer(ItemStack.class, new ItemStackKeyDeserializer())
        .addSerializer(ItemStack.class, new ItemStackSerializer())
        .addDeserializer(ItemStack.class, new ItemStackDeserializer());
  }

  @Override
  public String getPath() {
    return PATH;
  }
}
