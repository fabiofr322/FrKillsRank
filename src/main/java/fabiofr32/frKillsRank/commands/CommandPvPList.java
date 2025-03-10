package fabiofr32.frKillsRank.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.PlayerDataManager;

import java.util.ArrayList;
import java.util.List;

public class CommandPvPList implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigManager.getSimpleMessage("settings.messages.only_players"));
            return true;
        }

        List<String> pvpOnList = new ArrayList<>();
        List<String> pvpOffList = new ArrayList<>();

        // Percorre todos os jogadores online e separa entre PvP ON e OFF
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerDataManager.isPvPEnabled(player)) {
                pvpOnList.add(player.getName());
            } else {
                pvpOffList.add(player.getName());
            }
        }

        // Obtém as mensagens do config.yml
        String header = ConfigManager.getSimpleMessage("settings.pvp.pvplist_header");
        String footer = ConfigManager.getSimpleMessage("settings.pvp.pvplist_footer");
        String pvpOnMsg = ConfigManager.getSimpleMessage("settings.pvp.pvplist_pvp_on")
                .replace("{players}", pvpOnList.isEmpty() ? ConfigManager.getSimpleMessage("settings.pvp.pvplist_none") : String.join(", ", pvpOnList));
        String pvpOffMsg = ConfigManager.getSimpleMessage("settings.pvp.pvplist_pvp_off")
                .replace("{players}", pvpOffList.isEmpty() ? ConfigManager.getSimpleMessage("settings.pvp.pvplist_none") : String.join(", ", pvpOffList));

        // Envia a mensagem para o jogador
        sender.sendMessage(header);
        sender.sendMessage(pvpOnMsg);
        sender.sendMessage(pvpOffMsg);
        sender.sendMessage(footer);

        return true;
    }
}
