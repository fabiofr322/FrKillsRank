package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MainGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked(); // Declaração adicionada

        String configTitle = FrKillsRank.getInstance().getConfig().getString("settings.gui.main_title", "&6FrKillsRank Menu");
        String strippedConfigTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', configTitle));
        String strippedInvTitle = ChatColor.stripColor(event.getView().getTitle());
        if (!strippedInvTitle.equals(strippedConfigTitle)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        switch (slot) {
            case 21:
                // Recompensas: abre o GUI de loja
                ShopGUI.openMainShop(player);
                break;
            case 22:
                // Rank: executa o comando que mostra o rank (por exemplo, /pointsrank)
                player.chat("/pointsranktop");
                break;
            case 23:
                // abre o menu de statisticas
                StatsGUI.openStatsGUI(player);
                break;
            case 24:
                // Missões: executa o comando que mostra as missões (por exemplo, /missions)
                player.chat("/missions");
                break;
            case 40:
            	// fechar menu
            	player.closeInventory();
            	break;
            default:
                break;
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String configTitle = FrKillsRank.getInstance().getConfig().getString("settings.gui.main_title", "&6FrKillsRank Menu");
        // Remove as cores para uma comparação mais confiável
        String strippedConfigTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', configTitle));
        String strippedInvTitle = ChatColor.stripColor(event.getView().getTitle());
        if (strippedInvTitle.equals(strippedConfigTitle)) {
            event.setCancelled(true);
        }
    }

}
