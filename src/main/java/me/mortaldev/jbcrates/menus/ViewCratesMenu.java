package me.mortaldev.jbcrates.menus;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.mortaldev.jbcrates.Main;
import me.mortaldev.jbcrates.modules.crate.Crate;
import me.mortaldev.jbcrates.modules.crate.CrateManager;
import me.mortaldev.jbcrates.modules.menu.InventoryButton;
import me.mortaldev.jbcrates.modules.menu.InventoryGUI;
import me.mortaldev.jbcrates.utils.ItemStackBuilder;
import me.mortaldev.jbcrates.utils.TextUtil;
import me.mortaldev.jbcrates.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ViewCratesMenu extends InventoryGUI {

  private int currentPage;
  private final List<Crate> crateList;
  private int inventorySize;
  private int adjustedSlots;

  public ViewCratesMenu(int page) {
    this.currentPage = page;
    this.crateList = CrateManager.getCrates();
    setupVars();
  }

  public ViewCratesMenu(int page, List<Crate> newCrateList) {
    this.currentPage = page;
    this.crateList = newCrateList;
    setupVars();
  }

  void setupVars(){
    this.inventorySize = Utils.clamp((int) Math.ceil((double) (crateList.size() - ((currentPage - 1) * 45)) / 9)+1, 3, 6);
    this.adjustedSlots = (inventorySize - 3) * 9;
  }

  int getMaxPage() {
    return (int) Math.ceil((double) crateList.size() / 45);
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, inventorySize * 9, TextUtil.format("&3&lView Crates"));
  }

  @Override
  public void decorate(Player player) {
    if (crateList != null && !crateList.isEmpty()) {
      for (int i = 0, j = (currentPage - 1) * 45;
           i < Utils.clamp(crateList.size() - ((currentPage - 1) * 45), 1, 45);
           i++, j++) {
        Crate crate = crateList.get(j);
        this.addButton(i, crateButton(crate));
      }
    }
    ItemStack whiteGlass = ItemStackBuilder.builder(Material.WHITE_STAINED_GLASS_PANE).name("&7").build();
    this.getInventory().setItem(19 + adjustedSlots, whiteGlass);
    this.getInventory().setItem(20 + adjustedSlots, whiteGlass);
    this.getInventory().setItem(24 + adjustedSlots, whiteGlass);
    this.getInventory().setItem(25 + adjustedSlots, whiteGlass);
    this.addButton(18 + adjustedSlots, backButton());
    this.addButton(21 + adjustedSlots, refreshButton());
    this.addButton(22 + adjustedSlots, searchButton());
    this.addButton(23 + adjustedSlots, addCrateButton());
    if (getMaxPage() > 1 && currentPage != getMaxPage()){
      this.addButton(26 + adjustedSlots, nextButton());
    } else {
      this.getInventory().setItem(26 + adjustedSlots, whiteGlass);
    }
    super.decorate(player);
  }

  private InventoryButton crateButton(Crate inputCrate) {
    return new InventoryButton()
        .creator(player -> CrateManager.generateDisplayCrateItemStack(inputCrate))
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              Main.getGuiManager().openGUI(new ManageCrateMenu(inputCrate), player);
            });
  }

  private InventoryButton nextButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.ARROW)
                    .name("&2&lNext")
                    .addLore("&7Click to view next page")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (currentPage < getMaxPage()) {
                currentPage++;
                Main.getGuiManager().openGUI(new ViewCratesMenu(currentPage), player);
              }
            });
  }

  private InventoryButton addCrateButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.LIME_DYE)
                    .name("&2&lAdd Crate")
                    .addLore("&7Click to create & add a new crate.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              player.getInventory().close();
              player.sendMessage("");
              player.sendMessage(TextUtil.format("&7(Lasts 20s) &3Enter the name for the crate:"));
              player.sendMessage("");
              promptMap.put(player, Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                promptMap.remove(player);
                player.sendMessage(TextUtil.format("&cPrompt expired."));
              }, 20 * 20L));
            });
  }
  public static Map<Player, Integer> promptMap = new HashMap<>();

  public static class AddCratePromptEvent implements Listener {

    @EventHandler
    public void addCratePrompt(AsyncChatEvent event){
      Player player = event.getPlayer();
      if (!promptMap.containsKey(player)) {
        return;
      }
      event.setCancelled(true);
      Bukkit.getScheduler().cancelTask(promptMap.remove(player));
      Component formattedMessage = TextUtil.format(TextUtil.componentToString(event.message()));
      Crate crate = new Crate(formattedMessage);
      CrateManager.addCrate(crate);
      player.sendMessage(TextUtil.format("&3Crate " + crate.getId() + " crated with name: "));
      player.sendMessage(formattedMessage);
      Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
        Main.getGuiManager().openGUI(new ManageCrateMenu(crate), player);
      });
    }
  }


  private InventoryButton searchButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.ANVIL)
                    .name("&f&lSearch")
                    .addLore("&7Click to search through crates.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              new AnvilGUI.Builder()
                  .plugin(Main.getInstance())
                  .title("Search for Crate")
                  .itemLeft(
                      ItemStackBuilder.builder(Material.CHEST).name("&7Name of Crate").build())
                  .onClick(
                      (slot, stateSnapshot) -> {
                        if (slot == 2) {
                          String text = stateSnapshot.getText();
                          List<Crate> newCrateList =
                              CrateManager.getCrates().stream()
                                  .filter(
                                      crate ->
                                          crate.getId().toLowerCase().contains(text)
                                              || PlainTextComponentSerializer.plainText().serialize(crate.getDisplayName()).toLowerCase().contains(text))
                                  .toList();
                          Main.getGuiManager().openGUI(new ViewCratesMenu(1, newCrateList), player);
                        }
                        return Collections.emptyList();
                      })
                  .open(player);
            });
  }

  private InventoryButton refreshButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackBuilder.builder(Material.BONE_MEAL)
                    .name("&f&lRefresh")
                    .addLore("&7Click to refresh crates.")
                    .addLore("&7")
                    .addLore("&c&lWARNING:")
                    .addLore("&fIf you delete or edit any crate files externally,")
                    .addLore("&fyou &cMUST&f refresh with this button.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              CrateManager.updateCratesList();
              Main.getGuiManager().openGUI(new ViewCratesMenu(1), player);
            });
  }

  private InventoryButton backButton() {
    return new InventoryButton()
        .creator(
            playerCreator ->
                ItemStackBuilder.builder(Material.ARROW)
                    .name("&c&lBack")
                    .addLore("&7Click to return to previous page")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (currentPage == 1) {
                Main.getGuiManager().openGUI(new InitialCratesMenu(), player);
              } else {
                currentPage--;
                Main.getGuiManager().openGUI(new ViewCratesMenu(currentPage), player);
              }
            });
  }
}
