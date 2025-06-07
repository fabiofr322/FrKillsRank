package fabiofr32.frKillsRank.events;

import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.KillCompetitionManager;
import fabiofr32.frKillsRank.managers.PlayerDataManager;
import fabiofr32.frKillsRank.managers.ScoreboardManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import static fabiofr32.frKillsRank.managers.PlayerDataManager.getPlayerDataConfig;
import static fabiofr32.frKillsRank.managers.PlayerDataManager.savePlayerData;

public class MobKillListener implements Listener {

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        Player player = event.getEntity().getKiller();
        EntityType mobType = event.getEntityType();

        // Verifica diretamente se o mob está configurado no config.yml
        if (ConfigManager.isMobConfigured(mobType.toString())) {

            PlayerDataManager.setLastMobKilled(player, event.getEntity().getType().toString());
            int currentKills = getPlayerDataConfig().getInt("players." + player.getUniqueId() + ".mobs." + mobType, 0);
            getPlayerDataConfig().set("players." + player.getUniqueId() + ".mobs." + mobType, currentKills + 1);
            savePlayerData();


            int points = ConfigManager.getMobPoints(mobType.toString());

            // Adiciona pontos e registra a kill
            ConfigManager.addPoints(player, points);
            ConfigManager.addKill(player);

            // Atualiza o Scoreboard
            ScoreboardManager.updateScoreboard(player);

            // Envia mensagem ao jogador
            /*/player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    ConfigManager.getMessage("messages.kill", player, points)));*/

            // Registra a kill para o evento de competição
            KillCompetitionManager.recordKill(player);
        }
    }
}
