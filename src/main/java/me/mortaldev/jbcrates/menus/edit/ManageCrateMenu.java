package me.mortaldev.jbcrates.menus.edit;

import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.listeners.ChatListener;
import me.mortaldev.jbcrates.menus.CratesMenu;
import me.mortaldev.jbcrates.menus.MenuData;
import me.mortaldev.jbcrates.menus.factories.ConfirmMenu;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateHandler;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.List;

public class ManageCrateMenu extends InventoryGUI {

  private final Crate crate;
  private final MenuData menuData;

  public ManageCrateMenu(Crate crate, MenuData menuData) {
    this.crate = crate;
    this.menuData = menuData;
  }

  @Override
  protected Inventory createInventory() {
    if (crate == null) {
      return Bukkit.createInventory(null, 3 * 9, TextUtil.format("&3&lEditing Crate: &f"));
    } else {
      return Bukkit.createInventory(
          null, 3 * 9, TextUtil.format("&3&lEditing Crate: &f" + crate.getId()));
    }
  }

  @Override
  public void decorate(Player player) {
    this.addButton(10, nameButton());
    this.addButton(12, descriptionButton());
    this.addButton(14, rewardsButton());
    this.addButton(16, deleteButton());
    this.addButton(0, backButton());
    super.decorate(player);
  }

  private InventoryButton backButton() {
    return new InventoryButton()
        .creator(
            playerCreator ->
                ItemStackHelper.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to previous page")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              GUIManager.getInstance().openGUI(new CratesMenu(menuData), player);
            });
  }

  private InventoryButton deleteButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.RED_DYE)
                    .name("&c&lDELETE:")
                    .addLore("&7")
                    .addLore("&cDelete crate &l" + crate.getId() + "&c?")
                    .addLore("&7")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ConfirmMenu confirmMenu =
                  new ConfirmMenu(
                      "&cDelete crate &l" + crate.getId() + "&c?",
                      CrateHandler.getInstance().generateDisplayCrateItemStack(crate),
                      (promptPlayer) -> {
                        CrateManager.getInstance().remove(crate, true);
                        GUIManager.getInstance().openGUI(new CratesMenu(menuData), promptPlayer);
                      },
                      (promptPlayer) -> GUIManager.getInstance()
                          .openGUI(new ManageCrateMenu(crate, menuData), promptPlayer));
              GUIManager.getInstance().openGUI(confirmMenu, player);
            });
  }

  private InventoryButton rewardsButton() {
    return new InventoryButton()
        .creator(
            player -> {
              List<String> crateRewardsText =
                  CrateHandler.getInstance().getCrateRewardsText(crate, 7);
              return ItemStackHelper.builder(Material.CHEST)
                  .name("&3&lREWARDS:")
                  .addLore("&7")
                  .addLore(crateRewardsText)
                  .addLore("&7")
                  .addLore("&e[EDIT]")
                  .build();
            })
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              GUIManager.getInstance().openGUI(new CrateRewardsMenu(crate, menuData), player);
            });
  }

  private InventoryButton descriptionButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.PAPER)
                    .name("&3&lDESCRIPTION:")
                    .addLore("&7")
                    .addLore(crate.getDescription())
                    .addLore("&7")
                    .addLore("&e[CHANGE]")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Crate Desc: " + crate.getId())
                  .itemLeft(
                      ItemStackHelper.builder(Material.PAPER).name(crate.getDescription()).build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          crate.setDescription(stateSnapshot.getText());
                          crate.modify();
                          CrateManager.getInstance().update(crate);
                          GUIManager.getInstance()
                              .openGUI(new ManageCrateMenu(crate, menuData), player);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  private InventoryButton nameButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.NAME_TAG)
                    .name("&3&lNAME:")
                    .addLore("&7")
                    .addLore(crate.getDisplayName())
                    .addLore("&7")
                    .addLore("&e[RENAME]")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.getInventory().close();
              player.sendMessage("");
              String originalName = crate.getDisplayName();
              Component component =
                  MiniMessage.miniMessage()
                      .deserialize(
                          "<hover:show_text:'<gray>Click for Original Text'><click:SUGGEST_COMMAND:'"
                              + originalName
                              + "'>[Original]</click></hover>")
                      .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
              player.sendMessage(
                  TextUtil.format("&7(Lasts 20s) &3Enter the new name for the crate: ")
                      .append(component));
              player.sendMessage("");
              ChatListener.makeRequest(
                  player,
                  20 * 20L,
                  (chatComponent) -> {
                    String string = TextUtil.chatComponentToString(chatComponent);
                    crate.setDisplayName(string);
                    crate.modify();
                    CrateManager.getInstance().update(crate);
                    GUIManager.getInstance().openGUI(new ManageCrateMenu(crate, menuData), player);
                  });
            });
  }
}
