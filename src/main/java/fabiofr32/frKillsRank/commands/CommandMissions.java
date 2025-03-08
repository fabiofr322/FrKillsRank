package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.MissionsManager;
import fabiofr32.frKillsRank.managers.MissionsManager.Mission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class CommandMissions implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Apenas jogadores podem usar o comando de missões
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.only_players", "&cApenas jogadores podem usar esse comando!")));
            return true;
        }
        Player player = (Player) sender;

        // Puxa as mensagens do config.yml
        String headerDaily = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.messages.missions_header_daily", "&6=== Missões Diárias ==="));
        String noneDaily = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.messages.missions_none_daily", "&cNenhuma missão diária disponível."));
        String headerWeekly = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.messages.missions_header_weekly", "&6=== Missões Semanais ==="));
        String noneWeekly = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.messages.missions_none_weekly", "&cNenhuma missão semanal disponível."));
        String missionFormat = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.messages.missions_format", "&a{mission} - Mate {amount} de {target} para receber {reward} pontos."));

        // Exibe as missões diárias
        player.sendMessage(headerDaily);
        if (MissionsManager.dailyMissions.isEmpty()) {
            player.sendMessage(noneDaily);
        } else {
            for (Map.Entry<String, Mission> entry : MissionsManager.dailyMissions.entrySet()) {
                Mission mission = entry.getValue();
                String msg = missionFormat.replace("{mission}", mission.getName())
                        .replace("{amount}", String.valueOf(mission.getAmount()))
                        .replace("{target}", mission.getTarget())
                        .replace("{reward}", String.valueOf(mission.getRewardPoints()));
                player.sendMessage(msg);
            }
        }

        // Exibe as missões semanais
        player.sendMessage(headerWeekly);
        if (MissionsManager.weeklyMissions.isEmpty()) {
            player.sendMessage(noneWeekly);
        } else {
            for (Map.Entry<String, Mission> entry : MissionsManager.weeklyMissions.entrySet()) {
                Mission mission = entry.getValue();
                String msg = missionFormat.replace("{mission}", mission.getName())
                        .replace("{amount}", String.valueOf(mission.getAmount()))
                        .replace("{target}", mission.getTarget())
                        .replace("{reward}", String.valueOf(mission.getRewardPoints()));
                player.sendMessage(msg);
            }
        }

        return true;
    }
}
