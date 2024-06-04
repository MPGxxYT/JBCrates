package me.mortaldev.jbcrates.menus;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.InventoryButton;
import me.mortaldev.jbcrates.modules.menu.InventoryGUI;
import me.mortaldev.jbcrates.records.Pair;
import me.mortaldev.jbcrates.utils.ItemStackHelper;
import me.mortaldev.jbcrates.utils.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageCrateMenu extends InventoryGUI {

  public static Map<Player, Pair<Crate, Integer>> setNamePromptMap = new HashMap<>();
  private final Crate crate;

  public ManageCrateMenu(Crate crate) {
    this.crate = crate;
  }

  @Override
  protected Inventory createInventory() {
    if (crate == null){
      return Bukkit.createInventory(
          null, 3 * 9, TextUtil.format("&3&lEditing Crate: &f"));
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
    this.addButton(18, backButton());
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
              Main.getGuiManager().openGUI(new ViewCratesMenu(1), player);
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
              Main.getGuiManager().openGUI(new ConfirmDeleteCrateMenu(crate), player);
            });
  }

  private InventoryButton rewardsButton() {
    return new InventoryButton()
        .creator(
            player -> {
              List<Component> crateRewardsText = CrateManager.getCrateRewardsText(crate, 7);
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
              Main.getGuiManager().openGUI(new CrateRewardsMenu(crate, crate.getOrder()), player);
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
                          CrateManager.updateCrate(crate.getId(), crate);
                          Main.getGuiManager().openGUI(new ManageCrateMenu(crate), player);
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
              String originalName = TextUtil.deformat(crate.getDisplayName());
              Component component =
                  MiniMessage.miniMessage()
                      .deserialize(
                          "<hover:show_text:'<gray>Click for Original Text'><click:SUGGEST_COMMAND:'"
                              + originalName
                              + "'>[Original]</click></hover>")
                      .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
              player.sendMessage(TextUtil.format("&7(Lasts 20s) &3Enter the new name for the crate: ").append(component));
              player.sendMessage("");
              setNamePromptMap.put(player, new Pair<>(crate, Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                setNamePromptMap.remove(player);
                player.sendMessage(TextUtil.format("&cPrompt expired."));
              }, 20 * 20L)));
            });
  }

  public static class SetCrateNamePromptEvent implements Listener {
    @EventHandler
    public void setCrateNamePrompt(AsyncChatEvent event){
      Player player = event.getPlayer();
      if (!setNamePromptMap.containsKey(player)) {
        return;
      }
      event.setCancelled(true);
      Crate crate = setNamePromptMap.get(player).first();
      Bukkit.getScheduler().cancelTask(setNamePromptMap.remove(player).second());

      String rewardString = event.originalMessage() instanceof TextComponent ? ((TextComponent) event.originalMessage()).content() : "";
      Component formattedText = TextUtil.format(rewardString);

      crate.setDisplayName(formattedText);
      CrateManager.updateCrate(crate.getId(), crate);
      player.sendMessage(TextUtil.format("&3Crate " + crate.getId() + " updated with name: "));
      player.sendMessage(formattedText);
      Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> Main.getGuiManager().openGUI(new ManageCrateMenu(crate), player));
    }
  }


}
