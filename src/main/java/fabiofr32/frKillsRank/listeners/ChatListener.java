package fabiofr32.frKillsRank.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.ChatColor;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Obtém o rank do jogador usando o método do ConfigManager
        String rankName = ConfigManager.getRank(player);
        String rankColor = ConfigManager.getSimpleMessage("settings.ranks.chat." + rankName);

// Se a cor do rank não estiver configurada, define um padrão
        if (rankColor == null || rankColor.isEmpty()) {
            rankColor = "&7[" + rankName + "]";
        }

// Traduz os códigos de cores e aplica ao formato do chat
        event.setFormat(ChatColor.translateAlternateColorCodes('&', rankColor) + " " + player.getName() + ": " + event.getMessage());


        // Define o formato do chat para incluir o rank colorido
        event.setFormat(ChatColor.translateAlternateColorCodes('&', rankColor) + " " + player.getName() + ": " + event.getMessage());
    }
}
