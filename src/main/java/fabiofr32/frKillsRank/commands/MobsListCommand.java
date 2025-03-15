package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class MobsListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigManager.getSimpleMessage("settings.mob_list.only_players"));
            return true;
        }

        Player player = (Player) sender;
        ConfigurationSection mobsSection = FrKillsRank.getInstance().getConfig().getConfigurationSection("settings.points");

        if (mobsSection == null) {
            player.sendMessage(ConfigManager.getSimpleMessage("settings.mob_list.no_data"));
            return true;
        }

        // Criamos uma lista de mobs e ordenamos pelo valor dos pontos
        Map<String, Integer> mobs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String mob : mobsSection.getKeys(false)) {
            mobs.put(mob, mobsSection.getInt(mob));
        }

        // Exibir a tabela formatada no chat com mensagens do config.yml
        player.sendMessage(ConfigManager.getSimpleMessage("settings.mob_list.top_header"));
        player.sendMessage(ChatColor.YELLOW + "Mob                 | Points");
        player.sendMessage(ChatColor.GRAY + "--------------------------------");

        for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
            String mobName = String.format("%-20s", entry.getKey()); // Formata o nome para alinhar
            String mobPoints = ChatColor.GREEN + String.valueOf(entry.getValue());

            player.sendMessage(ChatColor.AQUA + mobName + " | " + mobPoints);
        }

        player.sendMessage(ChatColor.GOLD + "=================================");

        return true;
    }
}

