package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import fabiofr32.frKillsRank.managers.PlayerDataManager;

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

        // Obtém o status do PvP
        boolean isPvPEnabled = PlayerDataManager.isPvPEnabled(player);
        String pvpStatus = isPvPEnabled ? "§aON" : "§cOFF"; // ON = Verde, OFF = Vermelho

        // Puxa as mensagens do config.yml e substitui as variáveis
        String rankLine = getConfigString("settings.scoreboard.rank", "&6🏆 Rank: &a%rank%")
                .replace("%rank%", rank);

        String killsLine = getConfigString("settings.scoreboard.kills", "&c💀 Kills: &f%kills%")
                .replace("%kills%", String.valueOf(kills));

        String pointsLine = getConfigString("settings.scoreboard.points", "&e⭐ Pontos: %points%")
                .replace("%points%", String.valueOf(points));

        // Se o jogador tem 0 pontos, exibir "N/A" em vez de "#1"
        String positionDisplay = (points == 0) ? "N/A" : ("" + rankPosition);

        String positionLine = getConfigString("settings.scoreboard.position", "&b📌 Posição: &a%position%")
                .replace("%position%", positionDisplay);

        // Linha do status do PvP
        String pvpLine = getConfigString("settings.scoreboard.pvp_status", "&d⚔ PvP: %status%")
                .replace("%status%", pvpStatus);

        // Adicionando espaçamentos alternados para forçar a exibição correta
        objective.getScore(ChatColor.GRAY + "-------------------").setScore(9);
        objective.getScore(rankLine).setScore(8);
        objective.getScore("  ").setScore(7);
        objective.getScore(positionLine).setScore(6);
        objective.getScore("   ").setScore(5);
        objective.getScore(killsLine).setScore(4);
        objective.getScore("    ").setScore(3);
        objective.getScore(pointsLine).setScore(2);
        objective.getScore("     ").setScore(1);
        objective.getScore(pvpLine).setScore(0); // PvP na última linha

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
