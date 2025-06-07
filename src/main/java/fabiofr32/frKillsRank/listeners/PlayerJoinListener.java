package fabiofr32.frKillsRank.listeners;

import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.PlayerDataManager;
import fabiofr32.frKillsRank.managers.ScoreboardManager;
import fabiofr32.frKillsRank.managers.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
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

        // Obtém o tempo salvo no arquivo
        long storedPlayTime = PlayerDataManager.getSavedPlayTime(player);

        // Obtém o tempo do Bukkit (estatísticas internas)
        long currentPlayTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long currentPlayTimeSeconds = currentPlayTimeTicks / 20;

        // Se o tempo salvo for menor que o tempo real, atualiza o valor no arquivo
        if (currentPlayTimeSeconds > storedPlayTime) {
            PlayerDataManager.savePlayTime(player);
        }

        // Debug
        Bukkit.getLogger().info("Tempo de jogo carregado para " + player.getName() + ": " + storedPlayTime + " segundos.");
    }

}
