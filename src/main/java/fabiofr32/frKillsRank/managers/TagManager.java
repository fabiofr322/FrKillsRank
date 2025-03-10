package fabiofr32.frKillsRank.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TagManager {

    /**
     * Atualiza a tag acima da cabeça do jogador SEM alterar o Scoreboard do Sidebar.
     * Usa o Main Scoreboard para garantir que todos vejam a tag corretamente.
     *
     * @param player O jogador a ser atualizado.
     */
    public static void updatePlayerTagOnScoreboard(Player target, Scoreboard scoreboard) {
        String rank = ConfigManager.getRank(target);
        String rankColor = ConfigManager.getSimpleMessage("settings.ranks.chat." + rank);
        if (rankColor == null || rankColor.isEmpty()) {
            rankColor = "&7[" + rank + "]";
        }
        rankColor = ChatColor.translateAlternateColorCodes('&', rankColor);

        // Obtém ou cria a equipe no scoreboard específico
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

}
