package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRemovePoints implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verifica permissão
        if (!sender.hasPermission("frkillsrank.removepoints")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.permission_denied", "&cVocê não tem permissão!")));
            return true;
        }

        // Verifica se os argumentos foram passados
        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.removepoints_usage", "&cUso: /removepoints <player> <amount>")));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.player_not_found", "&cJogador {player} não encontrado.")
                            .replace("{player}", targetName)));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.invalid_number", "&cNúmero inválido.")));
            return true;
        }

        int currentPoints = ConfigManager.getPoints(target);
        if (currentPoints < amount) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.removepoints_insufficient", "&cO jogador {player} não possui pontos suficientes para remover {points}.")
                            .replace("{player}", target.getName())
                            .replace("{points}", String.valueOf(amount))));
            return true;
        }

        // Remove os pontos utilizando ConfigManager (adicionando um valor negativo)
        ConfigManager.addPoints(target, -amount);

        // Mensagem para quem executou o comando
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.messages.removepoints_success", "&aRemovido {points} pontos de {player}.")
                        .replace("{points}", String.valueOf(amount))
                        .replace("{player}", target.getName())));
        // Mensagem para o jogador alvo
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.messages.points_removed", "&cForam removidos {points} pontos de você.")
                        .replace("{points}", String.valueOf(amount))));

        return true;
    }
}
