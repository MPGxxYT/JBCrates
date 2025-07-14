package me.mortaldev.jbcrates.modules.crate;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.crudapi.loading.AutoRegister;
import me.mortaldev.crudapi.loading.CRUDRegistry;
import me.mortaldev.jbcrates.Main;

@AutoRegister
public class CrateManager extends CRUDManager<Crate> {
  private static class Singleton {
    private static final CrateManager INSTANCE = new CrateManager();
  }

  public static CrateManager getInstance() {
    return Singleton.INSTANCE;
  }

  private CrateManager() {
    CRUDRegistry.getInstance().register(this);
  }

  @Override
  public CRUD<Crate> getCRUD() {
    return CrateCRUD.getInstance();
  }

  @Override
  public void log(String string) {
    Main.log(string);
  }

  public enum Order {
    ASCENDING("Ascending"),
    DESCENDING("Descending");

    private final String name;

    Order(String name) {
      this.name = name;
    }

    public static Order flip(Order order) {
      switch (order) {
        case ASCENDING -> {
          return DESCENDING;
        }
        case DESCENDING -> {
          return ASCENDING;
        }
      }
      return ASCENDING;
    }

    public String getName() {
      return name;
    }
  }

  public enum SortBy {
    CHANCE("Chance"),
    DISPLAY_NAME("Display Name"),
    ITEM("Item Name"),
    CUSTOM("Custom");

    private final String name;

    SortBy(String name) {
      this.name = name;
    }

    public static SortBy next(SortBy sortBy) {
      for (int i = 0; i < values().length; i++) {
        SortBy value = values()[i];
        if (value == sortBy) {
          if (i + 1 >= values().length) {
            return values()[0];
          } else {
            return values()[i + 1];
          }
        }
      }
      return values()[0];
    }

    public String getName() {
      return name;
    }
  }
}
