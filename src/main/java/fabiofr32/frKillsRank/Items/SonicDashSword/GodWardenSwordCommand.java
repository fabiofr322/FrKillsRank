package fabiofr32.frKillsRank.Items.SonicDashSword;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GodWardenSwordCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,String[] args) {
        if (!(sender instanceof Player player)) {
           return true;
        }
        player.getInventory().addItem(GodwardenSwordItem.create(FrKillsRank.getInstance()));
        return true;

    }
}
