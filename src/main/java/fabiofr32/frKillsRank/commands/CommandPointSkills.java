package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPointSkills implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String onlyPlayersMsg = FrKillsRank.getInstance().getConfig()
                    .getString("settings.messages.only_players", "Apenas jogadores podem usar este comando!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onlyPlayersMsg));
            return true;
        }
        Player player = (Player) sender;
        int points = ConfigManager.getPoints(player);
        String message = FrKillsRank.getInstance().getConfig()
                .getString("settings.messages.points_info", "Você possui {points} pontos.")
                .replace("{points}", String.valueOf(points));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return true;
    }
}
