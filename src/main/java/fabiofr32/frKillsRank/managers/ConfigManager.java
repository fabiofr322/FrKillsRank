package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static fabiofr32.frKillsRank.managers.PlayerDataManager.getPlayerDataConfig;
import static fabiofr32.frKillsRank.managers.PlayerDataManager.savePlayerData;


public class ConfigManager {

    private static final HashMap<UUID, Integer> playerPoints = new HashMap<>();
    private static FileConfiguration config = FrKillsRank.getInstance().getConfig();
    private static String currentTop1 = "";

    public static void reloadConfig() {
        FrKillsRank plugin = FrKillsRank.getInstance();

        // Recarrega o config atual do disco
        plugin.reloadConfig();

        // Copia valores padrão (do config.yml interno) para o arquivo se estiverem ausentes
        plugin.getConfig().options().copyDefaults(true);

        // Salva o config atualizado com os valores faltantes inseridos
        plugin.saveConfig();

        // Atualiza o cache interno
        config = plugin.getConfig();
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

        // Obtém o streak atual
        int currentStreak = PlayerDataManager.getStreak(player);
        int newStreak = currentStreak + 1;

        PlayerDataManager.checkAndUpdateBestStreak(player, newStreak);

        // Atualiza o streak
        PlayerDataManager.setStreak(player, newStreak);

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
        int position = 1;

        if (getPlayerDataConfig().contains("players")) {
            ConfigurationSection playersSection = getPlayerDataConfig().getConfigurationSection("players");
            for (String key : playersSection.getKeys(false)) {
                // Ignora o próprio jogador (compara o UUID em formato de String)
                if (!key.equals(player.getUniqueId().toString())) {
                    int otherPoints = getPlayerDataConfig().getInt("players." + key + ".points", 0);
                    if (otherPoints > playerPts) {
                        position++;
                    }
                }
            }
        }
        return position;
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
            // Ignora a chave "chat"
            if (rankName.equalsIgnoreCase("chat")) continue;

            int requiredPoints = ranksSection.getInt(rankName);
            if (points >= requiredPoints && requiredPoints >= currentPoints) {
                currentRank = rankName;
                currentPoints = requiredPoints;
            }
        }
        return currentRank;
    }

    public static void checkAndUpdateTop1() {
        OfflinePlayer topPlayer = getTopRankPlayerOffline();
        if (topPlayer == null || topPlayer.getName() == null) return;

        String newTop1 = topPlayer.getName();

        // Se o Top 1 mudou (comparando de forma sensível a maiúsculas/minúsculas)
        if (!newTop1.equals(currentTop1)) {
            currentTop1 = newTop1; // Atualiza o Top 1

            // Se o novo Top 1 estiver online, ativa o PvP para ele
            if (topPlayer.isOnline()) {
                Player onlineTop = (Player) topPlayer;
                PlayerDataManager.setPvP(onlineTop, true);
            }

            // Envia a mensagem de broadcast se estiver habilitado nas configurações
            if (config.getBoolean("settings.top1.enable", true)) {
                String titleMessage = config.getString("settings.top1.broadcast_title", "&6🔥 {player} DOMINOU O RANK! 🔥");
                String subtitleMessage = config.getString("settings.top1.broadcast_subtitle", "&e👑 O novo REI dos pontos foi coroado! 👑");

                // Substitui o placeholder {player} pelo nome do novo Top 1
                titleMessage = titleMessage.replace("{player}", newTop1);
                subtitleMessage = subtitleMessage.replace("{player}", newTop1);

                // Envia o título para todos os jogadores online
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', titleMessage),
                            ChatColor.translateAlternateColorCodes('&', subtitleMessage),
                            10, 70, 20);
                }
            }
        }
    }

    public static Player getTopRankPlayer() {
        Player topPlayer = null;
        int highestPoints = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            int playerPoints = getPoints(p);
            if (playerPoints > highestPoints) {
                highestPoints = playerPoints;
                topPlayer = p;
            }
        }
        return topPlayer;
    }

    public static OfflinePlayer getTopRankPlayerOffline() {
        if (getPlayerDataConfig().contains("players")) {
            ConfigurationSection playersSection = getPlayerDataConfig().getConfigurationSection("players");
            OfflinePlayer top = null;
            int maxPoints = -1;
            for (String key : playersSection.getKeys(false)) {
                int points = getPlayerDataConfig().getInt("players." + key + ".points", 0);
                if (points > maxPoints) {
                    maxPoints = points;
                    try {
                        UUID uuid = UUID.fromString(key);
                        top = Bukkit.getOfflinePlayer(uuid);
                    } catch (IllegalArgumentException e) {
                        // Ignora chaves que não são UUID válidos
                    }
                }
            }
            return top;
        }
        return null;
    }


}
