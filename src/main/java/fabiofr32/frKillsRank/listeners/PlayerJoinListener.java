package fabiofr32.frKillsRank.listeners;

import fabiofr32.frKillsRank.managers.ScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ScoreboardManager.updateScoreboard(event.getPlayer());
    }
}
