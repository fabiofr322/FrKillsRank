package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {

    public static void updateScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();

        if (bukkitManager == null) {
            Bukkit.getLogger().warning("O ScoreboardManager do Bukkit retornou null! Não foi possível atualizar o Scoreboard.");
            return;
        }

        Scoreboard scoreboard = bukkitManager.getNewScoreboard();
        // Puxa o título do Scoreboard do config.yml
        String title = getConfigString("settings.scoreboard.title", "&6&lKillsRank");
        Objective objective = scoreboard.registerNewObjective("killsrank", Criteria.DUMMY, title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Obtendo os dados do jogador
        String rank = ConfigManager.getRank(player);
        int kills = PlayerDataManager.getKills(player);
        int points = ConfigManager.getPoints(player);
        int rankPosition = ConfigManager.getPlayerRankingPosition(player);

        // Puxa as mensagens do config.yml e substitui as variáveis
        String rankLine = getConfigString("settings.scoreboard.rank", "&6🏆 Rank: &a%rank%")
                .replace("%rank%", rank);

        String killsLine = getConfigString("settings.scoreboard.kills", "&c💀 Kills: &f%kills%")
                .replace("%kills%", String.valueOf(kills));

        String pointsLine = getConfigString("settings.scoreboard.points", "&e⭐ Pontos: %points%")
                .replace("%points%", String.valueOf(points));

        String positionLine = getConfigString("settings.scoreboard.position", "&b📌 Posição: &a#%position%")
                .replace("%position%", String.valueOf(rankPosition));

        // Adicionando espaçamentos alternados para forçar a exibição correta
        objective.getScore(ChatColor.GRAY + "-------------------").setScore(8);
        objective.getScore(rankLine).setScore(7);
        objective.getScore("  ").setScore(6);
        objective.getScore(positionLine).setScore(5);
        objective.getScore("   ").setScore(4);
        objective.getScore(killsLine).setScore(3);
        objective.getScore("    ").setScore(2);
        objective.getScore(pointsLine).setScore(1);
        objective.getScore(ChatColor.GRAY + "--------------------").setScore(0);

        player.setScoreboard(scoreboard);
    }

    private static String getConfigString(String path, String defaultValue) {
        return ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString(path, defaultValue));
    }

    public static void clearScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();

        if (bukkitManager == null) {
            Bukkit.getLogger().warning("O ScoreboardManager do Bukkit retornou null! Não foi possível limpar o Scoreboard.");
            return;
        }

        Scoreboard scoreboard = bukkitManager.getNewScoreboard();
        player.setScoreboard(scoreboard);
    }
}
