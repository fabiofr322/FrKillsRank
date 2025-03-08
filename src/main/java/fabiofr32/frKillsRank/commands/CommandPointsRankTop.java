package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            String onlyPlayersMsg = FrKillsRank.getInstance().getConfig()
                    .getString("settings.messages.only_players", "Apenas jogadores podem usar este comando!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onlyPlayersMsg));
            return true;
        }
        Player player = (Player) sender;
        FileConfiguration config = FrKillsRank.getInstance().getConfig();
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) {
            String noDataMsg = config.getString("settings.messages.no_data", "Nenhum dado encontrado.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', noDataMsg));
            return true;
        }

        List<PlayerRank> ranks = new ArrayList<>();
        for (String key : playersSection.getKeys(false)) {
            int pts = config.getInt("players." + key + ".points", 0);
            try {
                UUID uuid = UUID.fromString(key);
                ranks.add(new PlayerRank(uuid, pts));
            } catch (IllegalArgumentException ex) {
                // Ignora entradas com UUID inválido.
            }
        }

        // Ordena em ordem decrescente de pontos.
        ranks.sort((a, b) -> Integer.compare(b.points, a.points));

        String header = config.getString("settings.messages.top_header", "=== Top 10 Jogadores ===");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        int limit = Math.min(10, ranks.size());
        for (int i = 0; i < limit; i++) {
            PlayerRank pr = ranks.get(i);
            String playerName = Bukkit.getOfflinePlayer(pr.uuid).getName();
            if (playerName == null) {
                playerName = pr.uuid.toString();
            }
            String line = config.getString("settings.messages.top_player", "{position}º - {player}: {points} pontos")
                    .replace("{position}", String.valueOf(i + 1))
                    .replace("{player}", playerName)
                    .replace("{points}", String.valueOf(pr.points));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
        return true;
    }
}
