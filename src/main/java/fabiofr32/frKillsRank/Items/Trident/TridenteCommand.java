package fabiofr32.frKillsRank.Items.Trident;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TridenteCommand implements CommandExecutor {

    private final FrKillsRank plugin;

    public TridenteCommand(FrKillsRank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            player.getInventory().addItem(TridenteUtils.criarTridente(plugin));
            player.sendMessage("§bVocê recebeu o §9Tridente da Maré Eterna§b!");
        }
        return true;
    }
}
