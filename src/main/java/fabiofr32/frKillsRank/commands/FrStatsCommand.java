package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.managers.PlayerDataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import fabiofr32.frKillsRank.managers.ConfigManager;

public class FrStatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verifica se o comando foi executado por um jogador
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigManager.getSimpleMessage("settings.messages.only_players"));
            return true;
        }

        Player player = (Player) sender;

// Obtém os dados do jogador através do ConfigManager e PlayerDataManager
        int totalPoints = PlayerDataManager.getPoints(player);
        int mobsKilled = PlayerDataManager.getKills(player);
        int bestStreak = PlayerDataManager.getStreak(player);
        int bestKillstreak = PlayerDataManager.getBestKillstreak(player);
        String lastMob = PlayerDataManager.getLastMobKilled(player);
        String mostKilledMob = PlayerDataManager.getMostKilledMob(player);
        int mostKilledMobCount = PlayerDataManager.getMostKilledMobCount(player);
        int deaths = PlayerDataManager.getDeaths(player);
        String lastDeath = PlayerDataManager.getLastDeath(player);
        long savedPlayTime = PlayerDataManager.getSavedPlayTime(player);
        String formattedPlayTime = PlayerDataManager.formatPlayTime(savedPlayTime);
        int pvpKills = PlayerDataManager.getPvPKills(player);
        int pvpDeaths = PlayerDataManager.getPvPDeaths(player);
        String kdRatio = PlayerDataManager.getKDRatio(player);
        double highestDamage = PlayerDataManager.getHighestDamage(player);
        double highestDamageTaken = PlayerDataManager.getHighestDamageTaken(player);

// Obtém as mensagens configuradas no config.yml e substitui os placeholders
        String title = ConfigManager.getSimpleMessage("settings.frstats.text.title").replace("{player}", player.getName());
        String totalPointsMsg = ConfigManager.getSimpleMessage("settings.frstats.text.totalPoints").replace("{points}", String.valueOf(totalPoints));
        String mobsKilledMsg = ConfigManager.getSimpleMessage("settings.frstats.text.mobsKilled").replace("{kills}", String.valueOf(mobsKilled));
        String bestStreakMsg = ConfigManager.getSimpleMessage("settings.frstats.text.bestStreak").replace("{streak}", String.valueOf(bestStreak));
        String bestKillstreakMsg = ConfigManager.getSimpleMessage("settings.frstats.text.bestKillstreak").replace("{streak}", String.valueOf(bestKillstreak));
        String lastMobMsg = ConfigManager.getSimpleMessage("settings.frstats.text.lastMob").replace("{mob}", lastMob);
        String mostKilledMobMsg = ConfigManager.getSimpleMessage("settings.frstats.text.mostKilledMob")
                .replace("{mob}", mostKilledMob)
                .replace("{kills}", String.valueOf(mostKilledMobCount));
        String deathsMsg = ConfigManager.getSimpleMessage("settings.frstats.text.deaths").replace("{deaths}", String.valueOf(deaths));
        String lastDeathMsg = ConfigManager.getSimpleMessage("settings.frstats.text.lastDeath").replace("{date}", lastDeath);
        String playTimeMsg = ConfigManager.getSimpleMessage("settings.frstats.text.playTime").replace("{value}", formattedPlayTime);
        String pvpKillsMsg = ConfigManager.getSimpleMessage("settings.frstats.text.pvpKills").replace("{kills}", String.valueOf(pvpKills));
        String pvpDeathsMsg = ConfigManager.getSimpleMessage("settings.frstats.text.pvpDeaths").replace("{deaths}", String.valueOf(pvpDeaths));
        String kdRatioMsg = ConfigManager.getSimpleMessage("settings.frstats.text.kdRatio").replace("{ratio}", kdRatio);
        String highestDamageMsg = ConfigManager.getSimpleMessage("settings.frstats.text.highestDamage").replace("{damage}", String.format("%.1f", highestDamage));
        String highestDamageTakenMsg = ConfigManager.getSimpleMessage("settings.frstats.text.highestDamageTaken").replace("{damage}", String.format("%.1f", highestDamageTaken));

// Envia as mensagens para o jogador
        player.sendMessage(title);
        player.sendMessage(totalPointsMsg);
        player.sendMessage(mobsKilledMsg);
        player.sendMessage(bestStreakMsg);
        player.sendMessage(bestKillstreakMsg);
        player.sendMessage(lastMobMsg);
        player.sendMessage(mostKilledMobMsg);
        player.sendMessage(deathsMsg);
        player.sendMessage(lastDeathMsg);
        player.sendMessage(formattedPlayTime);
        player.sendMessage(pvpKillsMsg);
        player.sendMessage(pvpDeathsMsg);
        player.sendMessage(kdRatioMsg);
        player.sendMessage(highestDamageMsg);
        player.sendMessage(highestDamageTakenMsg);


        return true;
    }
}
