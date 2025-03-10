package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
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
        // Salva os pontos via PlayerDataManager
        PlayerDataManager.addPoints(player, points);
    }

    public static int getPoints(Player player) {
        if (player == null) return 0; // Evita erro de NullPointerException
        return PlayerDataManager.getPoints(player);
    }


    public static void removePoints(Player player, int points) {
        int currentPoints = getPoints(player);
        int newPoints = Math.max(0, currentPoints - points);
        PlayerDataManager.setPoints(player, newPoints);
    }

    public static String getMessage(String key, Player player, int points) {
        String message = config.getString("settings." + key, "&cMensagem não encontrada: " + key);
        if (message == null) {
            return "&cMensagem não encontrada: " + key;
        }
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
        return config.getInt("settings.points." + mobType, 1);
    }

    public static int getKills(Player player) {
        return PlayerDataManager.getKills(player);
    }

    public static void addKill(Player player) {
        // Incrementa a kill do jogador
        PlayerDataManager.addKill(player);
        // Atualiza o scoreboard com as novas informações, incluindo a posição
        ScoreboardManager.updateScoreboard(player);
    }


    public static String getRank(Player player) {
        int points = getPoints(player);
        ConfigurationSection ranksSection = config.getConfigurationSection("settings.ranks");

        if (ranksSection == null) return "Sem Rank";

        String currentRank = "Sem Rank";
        int currentPoints = 0;

        for (String rankName : ranksSection.getKeys(false)) {
            // Verifica se a chave não é "chat" antes de processar
            if (rankName.equalsIgnoreCase("chat")) continue;

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

        // Se o jogador tem pontos maiores que 0, usamos a lógica normal
        if (playerPts > 0) {
            int position = 1;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player) && getPoints(p) > playerPts) {
                    position++;
                }
            }
            return position;
        } else {
            // Se o jogador tem 0 pontos, verifica se existe alguém com mais de 0
            boolean someoneHasMore = false;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player) && getPoints(p) > 0) {
                    someoneHasMore = true;
                    break;
                }
            }
            if (someoneHasMore) {
                // A posição do jogador com 0 pontos será: número de jogadores com >0 pontos + 1
                int count = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (getPoints(p) > 0) {
                        count++;
                    }
                }
                return count + 1;
            } else {
                // Se todos tiverem 0 pontos, a posição será 1
                return 1;
            }
        }
    }



    // Método atualizado para utilizar a chave completa sem adicionar prefixos extras
    public static List<String> getMessageList(String key) {
        if (!config.contains(key)) {
            return Collections.emptyList();
        }
        return config.getStringList(key).stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }

    public static int getRankPoints(String rank) {
        return config.getInt("settings.ranks." + rank, -1);
    }

    public static boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public static void setBoolean(String path, boolean value) {
        config.set(path, value);
        FrKillsRank.getInstance().saveConfig();
    }

    public static String getRankForPoints(int points) {
        ConfigurationSection ranksSection = FrKillsRank.getInstance().getConfig().getConfigurationSection("settings.ranks");
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

    public static void checkAndUpdateTop1(Player player) {
        // Se o jogador for Top 1, ativamos o PvP automaticamente
        if (getPlayerRankingPosition(player) == 1) {
            PlayerDataManager.setPvP(player, true);
        }
    }

}
