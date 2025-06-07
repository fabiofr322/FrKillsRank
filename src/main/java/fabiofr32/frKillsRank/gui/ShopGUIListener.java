package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.managers.ConfigManager;
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
        String shopTitle = ChatColor.translateAlternateColorCodes('&',
                ConfigManager.getSimpleMessage("settings.shop_gui.title"));

        if (event.getView().getTitle().equals(shopTitle)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            switch (clickedItem.getType()) {
                case GOLDEN_APPLE:
                    player.sendMessage(ChatColor.GREEN + "Abrindo loja de consumíveis...");
                    ConsumablesGUI.openConsumablesShop(player);
                    break;

                case DIAMOND_CHESTPLATE:
                    player.sendMessage(ChatColor.GREEN + "Abrindo loja de armaduras...");
                    ArmorsGUI.openArmorsShop(player);
                    break;

                case DIAMOND_SWORD:
                    player.sendMessage(ChatColor.GREEN + "Abrindo loja de espadas...");
                    SwordsGUI.openSwordsShop(player);
                    break;

                case TOTEM_OF_UNDYING:
                    player.sendMessage(ChatColor.GREEN + "Abrindo loja de itens de suporte...");
                    SupportItemsGUI.openSupportItemsShop(player);
                    break;

                case ENCHANTED_BOOK:
                    if (ConfigManager.getPoints(player) >= ConfigManager.getRankPoints("Mestre")) {
                        player.sendMessage(ChatColor.GOLD + "Abrindo loja Mestre...");
                        MasterShopGUI.openMasterShop(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Você precisa de mais pontos para acessar esta loja!");
                    }
                    break;

                case NETHER_STAR:
                    if (ConfigManager.getPoints(player) >= ConfigManager.getRankPoints("Lendário")) {
                        player.sendMessage(ChatColor.AQUA + "Abrindo loja Lendária...");
                        LegendaryShopGUI.openLegendaryShop(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Você precisa de mais pontos para acessar esta loja!");
                    }
                    break;

                case BARRIER:
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "Você fechou a loja.");
                    break;

                default:
                    break;
            }
        }
    }
}
