package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class RewardsManager {

    private static File rewardsFile;
    private static FileConfiguration rewardsConfig;
    private static final Map<Integer, String> rewardsMap = new HashMap<>();

    public static void loadRewards() {
        rewardsFile = new File(FrKillsRank.getInstance().getDataFolder(), "rewards.yml");

        if (!rewardsFile.exists()) {
            FrKillsRank.getInstance().saveResource("rewards.yml", false);
        }

        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
        rewardsMap.clear();

        for (String key : rewardsConfig.getConfigurationSection("rewards").getKeys(false)) {
            rewardsMap.put(Integer.parseInt(key), key);
        }
    }

    public static boolean claimReward(Player player, int points) {
        if (!rewardsMap.containsKey(points)) {
            String noReward = ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.rewards.no_reward", "§cNenhuma recompensa disponível para essa pontuação!"));
            player.sendMessage(noReward);
            return false;
        }

        String path = "rewards." + rewardsMap.get(points);
        if (rewardsConfig.contains(path + ".item")) {
            Material material = Material.valueOf(rewardsConfig.getString(path + ".item"));
            int amount = rewardsConfig.getInt(path + ".amount", 1);
            player.getInventory().addItem(new ItemStack(material, amount));
            String itemRewardMsg = ChatColor.translateAlternateColorCodes('&',
                            FrKillsRank.getInstance().getConfig().getString("settings.rewards.item_reward", "§aVocê recebeu {amount}x {item}!"))
                    .replace("{amount}", String.valueOf(amount))
                    .replace("{item}", material.name());
            player.sendMessage(itemRewardMsg);
        }

        if (rewardsConfig.contains(path + ".money")) {
            int money = rewardsConfig.getInt(path + ".money");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + money);
            String moneyRewardMsg = ChatColor.translateAlternateColorCodes('&',
                            FrKillsRank.getInstance().getConfig().getString("settings.rewards.money_reward", "§aVocê recebeu ${money}!"))
                    .replace("{money}", String.valueOf(money));
            player.sendMessage(moneyRewardMsg);
        }

        if (rewardsConfig.contains(path + ".command")) {
            String command = rewardsConfig.getString(path + ".command").replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            String commandRewardMsg = ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.rewards.command_reward", "§aVocê recebeu uma recompensa especial!"));
            player.sendMessage(commandRewardMsg);
        }

        return true;
    }

    public static List<Integer> getAvailableRewards(Player player) {
        int points = ConfigManager.getPoints(player);
        List<Integer> availableRewards = new ArrayList<>();

        // Itera sobre os thresholds definidos no rewards.yml
        for (Integer rewardThreshold : rewardsMap.keySet()) {
            if (points >= rewardThreshold) {
                availableRewards.add(rewardThreshold);
            }
        }

        // Ordena as recompensas em ordem crescente
        Collections.sort(availableRewards);
        return availableRewards;
    }
}
