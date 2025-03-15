package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TopRankManager {

    // Armazena o UUID do jogador que atualmente está no Top 1
    private static UUID currentTop1 = null;

    /**
     * Verifica os dados do playerdata.yml e, se o Top 1 mudou,
     * atualiza o currentTop1, ativa o PvP para o novo Top 1 (se online)
     * e envia a mensagem de broadcast para todos os jogadores online.
     */
    public static void checkAndBroadcastTopRank() {
        OfflinePlayer topPlayer = ConfigManager.getTopRankPlayerOffline();
        if (topPlayer == null || topPlayer.getUniqueId() == null) return;

        UUID newTop1 = topPlayer.getUniqueId();

        // Se o Top 1 mudou (compara pelo UUID)
        if (currentTop1 == null || !newTop1.equals(currentTop1)) {
            currentTop1 = newTop1; // Atualiza o Top 1

            // Se o novo Top 1 estiver online, ativa o PvP para ele
            if (topPlayer.isOnline()) {
                Player onlineTop = (Player) topPlayer;
                PlayerDataManager.setPvP(onlineTop, true);
            }

            // Envia a mensagem de broadcast, se estiver habilitado no config
            FileConfiguration config = FrKillsRank.getInstance().getConfig();
            if (config.getBoolean("settings.top1.enable", true)) {
                String titleMessage = config.getString("settings.top1.broadcast_title", "&6🔥 {player} DOMINOU O RANK! 🔥");
                String subtitleMessage = config.getString("settings.top1.broadcast_subtitle", "&e👑 O novo REI dos pontos foi coroado! 👑");

                // Substitui {player} pelo nome do novo Top 1
                String newTopName = topPlayer.getName();
                titleMessage = titleMessage.replace("{player}", newTopName);
                subtitleMessage = subtitleMessage.replace("{player}", newTopName);

                // Envia o título para todos os jogadores online
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', titleMessage),
                            ChatColor.translateAlternateColorCodes('&', subtitleMessage),
                            10, 70, 20);
                }
            }
        }
    }
}
