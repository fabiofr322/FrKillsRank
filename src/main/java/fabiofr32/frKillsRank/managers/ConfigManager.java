package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {

    private static final HashMap<UUID, Integer> playerPoints = new HashMap<>();
    private static FileConfiguration config = FrKillsRank.getInstance().getConfig();

    public static void reloadConfig() {
        FrKillsRank.getInstance().reloadConfig();
        config = FrKillsRank.getInstance().getConfig();
    }

    public static void addPoints(Player player, int points) {
        PlayerDataManager.addPoints(player, points); // Agora salva no playerdata.yml
    }

    public static int getPoints(Player player) {
        return PlayerDataManager.getPoints(player); // Agora pega do playerdata.yml
    }

    public static void removePoints(Player player, int points) {
        int currentPoints = getPoints(player);
        int newPoints = Math.max(0, currentPoints - points); // Garante que não fique negativo
        PlayerDataManager.setPoints(player, newPoints); // Agora salva no playerdata.yml
    }


    public static String getMessage(String key, Player player, int points) {
        String message = config.getString("settings." + key, "&cMensagem não encontrada: " + key);

        if (message == null) {
            return "&cMensagem não encontrada: " + key;
        }

        // Substituir placeholders corretamente
        message = message.replace("{points}", String.valueOf(points));
        message = message.replace("{total_points}", (player != null) ? String.valueOf(getPoints(player)) : "0");

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getSimpleMessage(String key) {
        if (config == null) {
            return "&c[Erro] Configuração não carregada!";
        }

        String message = config.getString(key, "&cMensagem não encontrada: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
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

    public static List<String> getMessageList(String key) {
        if (!config.contains("settings.messages." + key)) {
            return Collections.emptyList(); // Retorna uma lista vazia se a chave não existir
        }
        return config.getStringList("settings.messages." + key).stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }



}
