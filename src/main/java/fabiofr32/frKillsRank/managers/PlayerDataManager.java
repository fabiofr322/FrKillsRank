package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerDataManager {

    private static File playerDataFile;
    private static FileConfiguration playerDataConfig;

    // Carrega ou cria o arquivo playerdata.yml
    public static void loadPlayerData() {
        playerDataFile = new File(FrKillsRank.getInstance().getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public static FileConfiguration getPlayerDataConfig() {
        return playerDataConfig;
    }

    // Salva as alterações no arquivo
    public static void savePlayerData() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Retorna os pontos do jogador (se não existir, retorna 0)
    public static int getPoints(Player player) {
        return playerDataConfig.getInt("players." + player.getUniqueId() + ".points", 0);
    }

    public static void setPoints(Player player, int points) {
        playerDataConfig.set("players." + player.getUniqueId() + ".points", points);
        savePlayerData();
        // Remova ou comente a chamada abaixo:
        // ConfigManager.checkAndUpdateTop1();
    }



    // Adiciona pontos ao jogador
    public static void addPoints(Player player, int points) {
        int currentPoints = getPoints(player);
        setPoints(player, currentPoints + points);
    }

    public static int getKills(Player player) {
        return playerDataConfig.getInt("players." + player.getUniqueId() + ".kills", 0);
    }

    public static void setKills(Player player, int kills) {
        playerDataConfig.set("players." + player.getUniqueId() + ".kills", kills);
        savePlayerData();
    }

    public static void addKill(Player player) {
        int currentKills = getKills(player);
        setKills(player, currentKills + 1);
    }

    public static void setPvP(Player player, boolean status) {
        playerDataConfig.set("players." + player.getUniqueId() + ".pvp", status);
        savePlayerData();
    }

    public static boolean isPvPEnabled(Player player) {
        return playerDataConfig.getBoolean("players." + player.getUniqueId() + ".pvp", false);
    }


    public static void setStreak(Player player, int streak) {
        getPlayerDataConfig().set("players." + player.getUniqueId() + ".streak", streak);
        savePlayerData();

        // Debug
        //Bukkit.getLogger().info("Streak atualizado para " + streak + " para o jogador " + player.getName());
    }

    // Inicializa um loop que atualiza o tempo de jogo automaticamente
    public static void startPlayTimeUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    savePlayTime(player);
                }
            }
        }.runTaskTimer(FrKillsRank.getInstance(), 20 * 60, 20 * 60); // A cada 60 segundos
    }

    public static void savePlayTime(Player player) {
        long currentPlayTimeSeconds = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
        long storedPlayTime = getSavedPlayTime(player);

        // Se o tempo atual for maior que o salvo, atualiza
        if (currentPlayTimeSeconds > storedPlayTime) {
            getPlayerDataConfig().set("players." + player.getUniqueId() + ".play_time", currentPlayTimeSeconds);
            savePlayerData();

            // Debug
            //Bukkit.getLogger().info("Tempo de jogo atualizado para " + player.getName() + ": " + currentPlayTimeSeconds + " segundos.");
        }
    }

    public static long getSavedPlayTime(Player player) {
        return getPlayerDataConfig().getLong("players." + player.getUniqueId() + ".play_time", 0);
    }

    public static String formatPlayTime(long seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        long secs = seconds % 60;

        return ConfigManager.getSimpleMessage("settings.frstats.text.playTime")
                .replace("{hours}", String.valueOf(hours))
                .replace("{minutes}", String.valueOf(minutes))
                .replace("{seconds}", String.valueOf(secs));
    }

    public static int getPvPKills(Player player) {
        return getPlayerDataConfig().getInt("players." + player.getUniqueId() + ".pvp_kills", 0);
    }

    public static void addPvPKill(Player player) {
        int kills = getPvPKills(player) + 1;
        getPlayerDataConfig().set("players." + player.getUniqueId() + ".pvp_kills", kills);
        savePlayerData();
    }

    public static int getPvPDeaths(Player player) {
        return getPlayerDataConfig().getInt("players." + player.getUniqueId() + ".pvp_deaths", 0);
    }

    public static void addPvPDeath(Player player) {
        int deaths = getPvPDeaths(player) + 1;
        getPlayerDataConfig().set("players." + player.getUniqueId() + ".pvp_deaths", deaths);
        savePlayerData();
    }

    public static String getKDRatio(Player player) {
        int kills = getPvPKills(player);
        int deaths = getPvPDeaths(player);

        if (deaths == 0) return String.valueOf(kills); // Evita divisão por zero
        double ratio = (double) kills / deaths;
        return String.format("%.2f", ratio);
    }

    public static double getHighestDamage(Player player) {
        return getPlayerDataConfig().getDouble("players." + player.getUniqueId() + ".highest_damage", 0);
    }

    public static void setHighestDamage(Player player, double damage) {
        double currentMax = getHighestDamage(player);
        if (damage > currentMax) {
            getPlayerDataConfig().set("players." + player.getUniqueId() + ".highest_damage", damage);
            savePlayerData();
        }
    }

    public static double getHighestDamageTaken(Player player) {
        return getPlayerDataConfig().getDouble("players." + player.getUniqueId() + ".highest_damage_taken", 0);
    }

    public static void setHighestDamageTaken(Player player, double damage) {
        double currentMax = getHighestDamageTaken(player);
        if (damage > currentMax) {
            getPlayerDataConfig().set("players." + player.getUniqueId() + ".highest_damage_taken", damage);
            savePlayerData();
        }
    }


    public static int getStreak(Player player) {
        return getPlayerDataConfig().getInt("players." + player.getUniqueId() + ".streak", 0);
    }



    public static void addKillToStreak(Player player) {
        int currentStreak = getStreak(player);
        int newStreak = currentStreak + 1;
        PlayerDataManager.setStreak(player, newStreak);
    }

    public static void resetStreak(Player player) {
        PlayerDataManager.setStreak(player, 0);
    }

    public static void setLastMobKilled(Player player, String mobType) {
        getPlayerDataConfig().set("players." + player.getUniqueId() + ".last_mob", mobType);
        savePlayerData();
    }

    public static String getLastMobKilled(Player player) {
        return getPlayerDataConfig().getString("players." + player.getUniqueId() + ".last_mob", "Nenhum");
    }

    public static int getBestKillstreak(Player player) {
        return getPlayerDataConfig().getInt("players." + player.getUniqueId() + ".best_streak", 0);
    }

    public static void checkAndUpdateBestStreak(Player player, int streak) {
        int bestStreak = getBestKillstreak(player);
        if (streak > bestStreak) {
            getPlayerDataConfig().set("players." + player.getUniqueId() + ".best_streak", streak);
            savePlayerData();
        }
    }

    public static String getMostKilledMob(Player player) {
        ConfigurationSection section = getPlayerDataConfig().getConfigurationSection("players." + player.getUniqueId() + ".mobs");

        // Caso o jogador nunca tenha matado um mob
        if (section == null) {
            return ConfigManager.getSimpleMessage("settings.frstats.no_most_killed_mob");
        }

        String topMob = ConfigManager.getSimpleMessage("settings.frstats.no_most_killed_mob");
        int maxKills = 0;

        for (String mob : section.getKeys(false)) {
            int kills = section.getInt(mob);
            if (kills > maxKills) {
                maxKills = kills;
                topMob = mob;
            }
        }

        // Retorna apenas o nome do mob
        return topMob;
    }

    public static int getMostKilledMobCount(Player player) {
        ConfigurationSection section = getPlayerDataConfig().getConfigurationSection("players." + player.getUniqueId() + ".mobs");

        if (section == null) {
            return 0;
        }

        int maxKills = 0;
        for (String mob : section.getKeys(false)) {
            int kills = section.getInt(mob);
            if (kills > maxKills) {
                maxKills = kills;
            }
        }
        return maxKills;
    }

    public static int getDeaths(Player player) {
        return getPlayerDataConfig().getInt("players." + player.getUniqueId() + ".deaths", 0);
    }

    public static void addDeath(Player player) {
        int currentDeaths = getDeaths(player);
        getPlayerDataConfig().set("players." + player.getUniqueId() + ".deaths", currentDeaths + 1);
        savePlayerData();
    }

    public static String getLastDeath(Player player) {
        return getPlayerDataConfig().getString("players." + player.getUniqueId() + ".last_death", "Nunca");
    }

    public static void setLastDeath(Player player) {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        getPlayerDataConfig().set("players." + player.getUniqueId() + ".last_death", date);
        savePlayerData();
    }



}
