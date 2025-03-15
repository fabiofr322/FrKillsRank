package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.PlayerDataManager;
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
            int points = 0;
            // Se o jogador estiver online, use o método existente; caso contrário, leia diretamente do playerdata.yml
            Player onlinePlayer = Bukkit.getPlayer(offlinePlayer.getUniqueId());
            if (onlinePlayer != null) {
                points = ConfigManager.getPoints(onlinePlayer);
            } else {
                points = PlayerDataManager.getPlayerDataConfig().getInt("players." + offlinePlayer.getUniqueId().toString() + ".points", 0);
            }
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
            String rankName = ConfigManager.getRankForPoints(pr.points);
            String rankFormatted = ConfigManager.getSimpleMessage("settings.ranks.chat." + rankName);

// Se o formato do chat não existir, usa apenas o nome do rank
            if (rankFormatted == null || rankFormatted.isEmpty()) {
                rankFormatted = rankName;
            }


            // Linha do Top
            String line = ChatColor.YELLOW.toString() + (i + 1) + "º " + ChatColor.AQUA + playerName + " " +
                    ChatColor.GRAY + "[" + rankFormatted + ChatColor.GRAY + "]" +
                    ChatColor.WHITE + " - " + ChatColor.GOLD + pr.points + " pontos";

            player.sendMessage(line);
        }
        return true;
    }
}
