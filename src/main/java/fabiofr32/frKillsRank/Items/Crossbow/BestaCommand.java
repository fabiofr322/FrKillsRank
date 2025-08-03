package fabiofr32.frKillsRank.Items.Crossbow;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BestaCommand implements CommandExecutor {

    private final FrKillsRank plugin;

    public BestaCommand(FrKillsRank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            player.getInventory().addItem(BestaUtils.criarBestaFumaca(plugin));
            player.sendMessage("§aVocê recebeu a §6Besta Explosiva§a!");
            return true;
        }
        return false;
    }
}