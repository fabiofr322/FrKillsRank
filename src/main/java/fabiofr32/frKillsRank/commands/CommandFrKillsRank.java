package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.gui.MainGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandFrKillsRank implements CommandExecutor, TabCompleter {

    // Lista de subcomandos disponíveis, incluindo os novos comandos para pontos
    private final List<String> subcommands = Arrays.asList(
            "gui",
            "pointskills",
            "pointsranktop",
            "reloadkillsconfig",
            "addpoints",
            "removepoints",
            "missions",
            "frloja",
            "pvp",
            "mobslist",
            "pvplist"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String onlyPlayersMsg = FrKillsRank.getInstance().getConfig()
                    .getString("settings.messages.only_players", "Apenas jogadores podem usar este comando!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onlyPlayersMsg));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            // Exibe a mensagem de ajuda com todos os subcomandos
            String helpHeader = FrKillsRank.getInstance().getConfig()
                    .getString("settings.messages.help_header", "&6Comandos disponíveis:");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', helpHeader));
            for (String sub : subcommands) {
                String helpLine = FrKillsRank.getInstance().getConfig()
                        .getString("settings.messages.help_line", "&a/frkillsrank {subcommand}");
                helpLine = helpLine.replace("{subcommand}", sub);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', helpLine));
            }
            return true;
        } else {
            String sub = args[0].toLowerCase();
            if (!subcommands.contains(sub)) {
                String invalidSub = FrKillsRank.getInstance().getConfig()
                        .getString("settings.messages.invalid_subcommand", "&cSubcomando inválido!");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidSub));
                return true;
            }

            // Prepara os argumentos para delegar o comando (removendo o subcomando da lista de argumentos)
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

            // Delegação para o comando específico
            switch (sub) {
                case "pointskills":
                    return new CommandPointSkills().onCommand(sender, command, label, newArgs);
                case "pointsranktop":
                    return new CommandPointsRankTop().onCommand(sender, command, label, newArgs);
                case "reloadkillsconfig":
                    return new CommandReloadConfig().onCommand(sender, command, label, newArgs);
                case "addpoints":
                    return new CommandAddPoints().onCommand(sender, command, label, newArgs);
                case "removepoints":
                    return new CommandRemovePoints().onCommand(sender, command, label, newArgs);
                case "missions":
                    return new CommandMissions().onCommand(sender, command, label, newArgs);
                case "frloja":
                    return new CommandShop().onCommand(sender, command, label, newArgs);
                case "pvp":
                    return new CommandPvP().onCommand(sender, command, label, newArgs);
                case "mobslist":
                    return new MobsListCommand().onCommand(sender, command, label, newArgs);
                // No switch do CommandFrKillsRank.java
                case "gui":
                    MainGUI.openMainGUI(player);
                    return true;
                default:
                    String invalidSubMsg = FrKillsRank.getInstance().getConfig()
                            .getString("settings.messages.invalid_subcommand", "&cSubcomando inválido!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidSubMsg));
                    return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (String sub : subcommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        return null;
    }
}
