package fabiofr32.frKillsRank.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.ChatColor;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deceased = event.getEntity();
        Player killer = deceased.getKiller();

        // Obtém o rank do jogador morto e sua cor do config.yml
        String deceasedRank = ConfigManager.getRank(deceased);
        String deceasedRankColor = ConfigManager.getSimpleMessage("settings.ranks.chat." + deceasedRank);

        // Se não houver configuração, usa um padrão
        if (deceasedRankColor == null || deceasedRankColor.isEmpty()) {
            deceasedRankColor = "&7[" + deceasedRank + "]";
        }
        deceasedRankColor = ChatColor.translateAlternateColorCodes('&', deceasedRankColor);

        // Define a mensagem de morte personalizada com a tag do rank
        event.setDeathMessage(deceasedRankColor + " " + deceased.getName() + " morreu!");

        // Se a morte foi causada por outro jogador, transfere os pontos e exibe o rank do assassino
        if (killer != null) {
            String killerRank = ConfigManager.getRank(killer);
            String killerRankColor = ConfigManager.getSimpleMessage("settings.ranks.chat." + killerRank);

            if (killerRankColor == null || killerRankColor.isEmpty()) {
                killerRankColor = "&7[" + killerRank + "]";
            }
            killerRankColor = ChatColor.translateAlternateColorCodes('&', killerRankColor);

            // Obtém os pontos e faz a transferência
            int deceasedPoints = ConfigManager.getPoints(deceased);
            int transferPoints = (int) Math.ceil(deceasedPoints * 0.05);
            ConfigManager.removePoints(deceased, transferPoints);
            ConfigManager.addPoints(killer, transferPoints);

            // Mensagens personalizadas do config.yml
            String deathLostMsg = ConfigManager.getSimpleMessage("settings.pvp.death_lost_points")
                    .replace("{points}", String.valueOf(transferPoints));
            String deathGainedMsg = ConfigManager.getSimpleMessage("settings.pvp.death_gained_points")
                    .replace("{points}", String.valueOf(transferPoints))
                    .replace("{player}", deceased.getName());

            // Envia as mensagens formatadas para os jogadores
            deceased.sendMessage(deathLostMsg);
            killer.sendMessage(deathGainedMsg);

            // Modifica a mensagem de morte para incluir o rank do assassino
            event.setDeathMessage(deceasedRankColor + " " + deceased.getName() + " foi morto por " + killerRankColor + " " + killer.getName() + "!");
        }
    }
}
