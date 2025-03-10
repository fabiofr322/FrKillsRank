package fabiofr32.frKillsRank.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.PlayerDataManager;

import java.util.ArrayList;
import java.util.List;

public class CommandPvP implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        // Verifica se o jogador é o Top 1
        if (ConfigManager.getPlayerRankingPosition(player) == 1 && ConfigManager.getPoints(player) > 0) {
            player.sendMessage("§cVocê é o Top 1 e não pode desativar o PvP!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUso correto: /pvp <on/off>");
            return true;
        }

        String option = args[0].toLowerCase();

        if (option.equals("on")) {
            PlayerDataManager.setPvP(player, true);
            player.sendMessage("§aPvP ativado!");
        } else if (option.equals("off")) {
            PlayerDataManager.setPvP(player, false);
            player.sendMessage("§cPvP desativado!");
        } else {
            player.sendMessage("§cUso correto: /pvp <on/off>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("on");
            suggestions.add("off");
        }

        return suggestions;
    }
}
