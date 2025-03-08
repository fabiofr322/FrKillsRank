package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigManager {

    private static final HashMap<UUID, Integer> playerPoints = new HashMap<>();
    private static FileConfiguration config = FrKillsRank.getInstance().getConfig();

    public static void reloadConfig() {
        FrKillsRank.getInstance().reloadConfig();
        config = FrKillsRank.getInstance().getConfig();
    }

    public static void addPoints(Player player, int points) {
        UUID uuid = player.getUniqueId();
        int newPoints = getPoints(player) + points;
        playerPoints.put(uuid, newPoints);
        config.set("players." + uuid + ".points", newPoints);
        FrKillsRank.getInstance().saveConfig();
    }

    public static int getPoints(Player player) {
        return playerPoints.getOrDefault(player.getUniqueId(), config.getInt("players." + player.getUniqueId() + ".points", 0));
    }

    public static String getMessage(String key, Player player, int points) {
        String message = config.getString("settings.messages." + key, key);
        message = message.replace("{points}", String.valueOf(points));

        // Se o player for null, substitui {total_points} por "0"
        if (player != null) {
            message = message.replace("{total_points}", String.valueOf(getPoints(player)));
        } else {
            message = message.replace("{total_points}", "0");
        }

        return message;
    }



    public static int getMobPoints(String mobType) {
        return config.getInt("settings.points." + mobType, 1); // Default 1 ponto caso não esteja configurado
    }

    public static int getKills(Player player) {
        return PlayerDataManager.getKills(player); // Pega direto do playerdata.yml
    }

    public static void addKill(Player player) {
        PlayerDataManager.addKill(player); // Adiciona e salva no playerdata.yml
    }



    public static String getRank(Player player) {
        int points = getPoints(player);

        ConfigurationSection ranksSection = config.getConfigurationSection("settings.ranks");
        if (ranksSection == null) return "Sem Rank";

        String currentRank = "Sem Rank";
        int currentPoints = 0;

        for (String rankName : ranksSection.getKeys(false)) {
            int requiredPoints = ranksSection.getInt(rankName);
            if (points >= requiredPoints && requiredPoints >= currentPoints) {
                currentRank = rankName;
                currentPoints = requiredPoints;
            }
        }

        return currentRank;
    }


    public static boolean isMobConfigured(String mobName) {
        return config.contains("settings.points." + mobName);
    }

    public static int getPlayerRankingPosition(Player player) {
        int playerPts = getPoints(player);
        int position = 1;
        // Percorre todos os jogadores presentes no cache de pontos
        for (UUID uuid : playerPoints.keySet()) {
            int pts = playerPoints.get(uuid);
            if (pts > playerPts) {
                position++;
            }
        }
        return position;
    }




}
