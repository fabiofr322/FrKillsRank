package fabiofr32.frKillsRank.listeners;

import fabiofr32.frKillsRank.managers.ScoreboardManager;
import fabiofr32.frKillsRank.managers.TagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Atualiza o scoreboard individual (Sidebar)
        ScoreboardManager.updateScoreboard(player);

        // Atualiza as tags de todos os jogadores em cada scoreboard individual
        TagManager.updateAllTags();
    }

}
