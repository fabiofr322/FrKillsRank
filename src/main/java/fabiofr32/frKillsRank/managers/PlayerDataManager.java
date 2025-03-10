package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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

    // Define os pontos do jogador e salva
    public static void setPoints(Player player, int points) {
        playerDataConfig.set("players." + player.getUniqueId() + ".points", points);
        savePlayerData();

        // Checa se o jogador se tornou Top 1 e ativa o PvP
        ConfigManager.checkAndUpdateTop1(player);
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



}
