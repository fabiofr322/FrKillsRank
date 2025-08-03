package fabiofr32.frKillsRank.Items.Cerejinha_sword;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SwordCherryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            p.getInventory().addItem(SwordCherry.criar());
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dVocê recebeu a Espada do Amor!"));
        }

        return false;
    }
}
