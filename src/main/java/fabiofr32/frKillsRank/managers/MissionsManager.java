package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MissionsManager {

    private static File missionsFile;
    private static FileConfiguration missionsConfig;

    // Armazena as missões diárias e semanais
    public static Map<String, Mission> dailyMissions = new HashMap<>();
    public static Map<String, Mission> weeklyMissions = new HashMap<>();

    public static void loadMissions() {
        missionsFile = new File(FrKillsRank.getInstance().getDataFolder(), "missions.yml");
        if (!missionsFile.exists()) {
            FrKillsRank.getInstance().saveResource("missions.yml", false);
        }
        missionsConfig = YamlConfiguration.loadConfiguration(missionsFile);
        dailyMissions.clear();
        weeklyMissions.clear();

        // Carrega missões diárias
        ConfigurationSection dailySection = missionsConfig.getConfigurationSection("missions.daily");
        if (dailySection != null) {
            for (String missionName : dailySection.getKeys(false)) {
                String target = dailySection.getString(missionName + ".target", "");
                int amount = dailySection.getInt(missionName + ".amount", 0);
                int reward = dailySection.getInt(missionName + ".reward_points", 0);
                Mission mission = new Mission(missionName, target, amount, reward);
                dailyMissions.put(missionName, mission);
            }
        }

        // Carrega missões semanais
        ConfigurationSection weeklySection = missionsConfig.getConfigurationSection("missions.weekly");
        if (weeklySection != null) {
            for (String missionName : weeklySection.getKeys(false)) {
                String target = weeklySection.getString(missionName + ".target", "");
                int amount = weeklySection.getInt(missionName + ".amount", 0);
                int reward = weeklySection.getInt(missionName + ".reward_points", 0);
                Mission mission = new Mission(missionName, target, amount, reward);
                weeklyMissions.put(missionName, mission);
            }
        }
    }

    // Classe interna que representa uma missão
    public static class Mission {
        private String name;
        private String target;
        private int amount;
        private int rewardPoints;

        public Mission(String name, String target, int amount, int rewardPoints) {
            this.name = name;
            this.target = target;
            this.amount = amount;
            this.rewardPoints = rewardPoints;
        }

        public String getName() {
            return name;
        }

        public String getTarget() {
            return target;
        }

        public int getAmount() {
            return amount;
        }

        public int getRewardPoints() {
            return rewardPoints;
        }
    }
}
