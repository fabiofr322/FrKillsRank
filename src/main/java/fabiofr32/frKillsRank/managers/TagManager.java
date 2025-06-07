package fabiofr32.frKillsRank.managers;

import org.bukkit.Bukkit;
import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TagManager {

    public static void updatePlayerTagOnScoreboard(Player target, Scoreboard scoreboard) {
        boolean usarTags = FrKillsRank.getInstance().getConfig().getBoolean("settings.use_rank_tags", true);
        if (!usarTags) return;

        String rank = ConfigManager.getRank(target);
        String rankColor = ConfigManager.getSimpleMessage("settings.ranks.chat." + rank);
        if (rankColor == null || rankColor.isEmpty()) {
            rankColor = "&7[" + rank + "]";
        }
        rankColor = ChatColor.translateAlternateColorCodes('&', rankColor);

        Team team = scoreboard.getTeam(target.getName());
        if (team == null) {
            team = scoreboard.registerNewTeam(target.getName());
        }
        team.setPrefix(rankColor + ChatColor.RESET + " ");
        team.addEntry(target.getName());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    public static void updateAllTags() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = viewer.getScoreboard();
            for (Player target : Bukkit.getOnlinePlayers()) {
                updatePlayerTagOnScoreboard(target, scoreboard);
            }
        }
    }

    public static String formatChat(Player player, String mensagemOriginal) {
        boolean usarTags = FrKillsRank.getInstance().getConfig().getBoolean("settings.use_rank_tags", true);
        String tag = "";

        if (usarTags) {
            String rank = ConfigManager.getRank(player);
            tag = ConfigManager.getSimpleMessage("settings.ranks.chat." + rank);
            if (tag == null || tag.isEmpty()) {
                tag = "&7[" + rank + "]";
            }
            tag = ChatColor.translateAlternateColorCodes('&', tag) + ChatColor.RESET + " ";
        }

        return tag + player.getName() + ": " + mensagemOriginal;
    }
}
