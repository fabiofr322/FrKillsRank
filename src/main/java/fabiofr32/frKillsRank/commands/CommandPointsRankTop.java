package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandPointsRankTop implements CommandExecutor {

    private static class PlayerRank {
        UUID uuid;
        int points;

        public PlayerRank(UUID uuid, int points) {
            this.uuid = uuid;
            this.points = points;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar este comando!");
            return true;
        }
        Player player = (Player) sender;

        // Pegar todos os jogadores e seus pontos
        List<PlayerRank> ranks = new ArrayList<>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            int points = ConfigManager.getPoints(Bukkit.getPlayer(offlinePlayer.getUniqueId()));

            if (points > 0) {
                ranks.add(new PlayerRank(offlinePlayer.getUniqueId(), points));
            }
        }

        // Ordenar por pontos (decrescente)
        ranks.sort((a, b) -> Integer.compare(b.points, a.points));

        // Mensagem de cabeçalho
        player.sendMessage(ChatColor.GOLD + "=== TOP 10 JOGADORES ===");

        int limit = Math.min(10, ranks.size());
        for (int i = 0; i < limit; i++) {
            PlayerRank pr = ranks.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(pr.uuid);
            String playerName = (offlinePlayer.getName() != null) ? offlinePlayer.getName() : pr.uuid.toString();
            String rank = ConfigManager.getRankForPoints(pr.points); // Obtém o rank do jogador

            // Linha do Top
            String line = ChatColor.YELLOW.toString() + (i + 1) + "º " + ChatColor.AQUA + playerName + " " +
                    ChatColor.GRAY + "[" + ChatColor.GREEN + rank + ChatColor.GRAY + "]" +
                    ChatColor.WHITE + " - " + ChatColor.GOLD + pr.points + " pontos";


            player.sendMessage(line);
        }
        return true;
    }
}
