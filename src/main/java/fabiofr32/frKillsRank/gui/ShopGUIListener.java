package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.gui.ShopGUI;
import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopGUIListener implements Listener {

    @EventHandler
    public void onShopGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String shopTitle = ChatColor.translateAlternateColorCodes('&', ConfigManager.getSimpleMessage("settings.shop_gui.title"));

        if (event.getView().getTitle().equals(shopTitle)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.GOLDEN_APPLE) {
                player.sendMessage(ChatColor.GREEN + "Abrindo loja de consumíveis...");
                ConsumablesGUI.openConsumablesShop(player);
            } else if (clickedItem.getType() == Material.DIAMOND_CHESTPLATE) {
                player.sendMessage(ChatColor.GREEN + "Abrindo loja de armaduras...");
                ArmorsGUI.openArmorsShop(player);
            } else if (clickedItem.getType() == Material.DIAMOND_SWORD) {
                player.sendMessage(ChatColor.GREEN + "Abrindo loja de espadas...");
                SwordsGUI.openSwordsShop(player);
            } else if (clickedItem.getType() == Material.TOTEM_OF_UNDYING) {
                player.sendMessage(ChatColor.GREEN + "Abrindo loja de itens de suporte...");
                SupportItemsGUI.openSupportItemsShop(player);
            } else if (clickedItem.getType() == Material.ENCHANTED_BOOK) {
                if (ConfigManager.getPoints(player) >= ConfigManager.getRankPoints("Mestre")) {
                    player.sendMessage(ChatColor.GOLD + "Abrindo loja Mestre...");
                    MasterShopGUI.openMasterShop(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Você precisa de mais pontos para acessar esta loja!");
                }
            } else if (clickedItem.getType() == Material.NETHER_STAR) {
                if (ConfigManager.getPoints(player) >= ConfigManager.getRankPoints("Lendário")) {
                    player.sendMessage(ChatColor.AQUA + "Abrindo loja Lendária...");
                    LegendaryShopGUI.openLegendaryShop(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Você precisa de mais pontos para acessar esta loja!");
                }
            }
        }
    }
}
