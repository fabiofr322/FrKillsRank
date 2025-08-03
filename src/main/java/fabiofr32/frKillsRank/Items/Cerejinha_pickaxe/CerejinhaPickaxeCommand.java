package fabiofr32.frKillsRank.Items.Cerejinha_pickaxe;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CerejinhaPickaxeCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        ((Player) sender).getInventory().addItem(CerejinhaPickaxe.getCerejinhaPickaxe());
        return true;
    }
}
