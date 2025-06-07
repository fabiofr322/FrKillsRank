package fabiofr32.frKillsRank.listeners;

import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.http.WebSocket;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.savePlayTime(player);
    }
}
