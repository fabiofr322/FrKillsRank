package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.KillCompetitionManager;
import fabiofr32.frKillsRank.managers.MissionsManager;
import fabiofr32.frKillsRank.managers.RewardsManager;
// Se existir um gerenciador para os dados dos jogadores, importe-o:
// import fabiofr32.frKillsRank.managers.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandReloadConfig implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Atualizamos a permissão para refletir o novo subcomando
        if (!sender.hasPermission("frkillsrank.reloadkillsconfig")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para executar esse comando!");
            return true;
        }

        // Recarrega o config.yml
        ConfigManager.reloadConfig();
        // Recarrega o rewards.yml
        RewardsManager.loadRewards();
        // Recarrega o missions.yml
        MissionsManager.loadMissions();
        // Recarrega o events.yml
        KillCompetitionManager.loadEventConfig();


        // Se houver um PlayerDataManager, recarregue também:
        // PlayerDataManager.loadPlayerData();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                ConfigManager.getMessage("reload", null, 0)));
        return true;
    }
}
