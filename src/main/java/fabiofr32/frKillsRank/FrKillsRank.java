package fabiofr32.frKillsRank;

import fabiofr32.frKillsRank.commands.*;
import fabiofr32.frKillsRank.events.MobKillListener;
import fabiofr32.frKillsRank.gui.MainGUIListener;
import fabiofr32.frKillsRank.gui.RewardsGUI;
import fabiofr32.frKillsRank.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrKillsRank extends JavaPlugin {

    private static FrKillsRank instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        RewardsManager.loadRewards(); // Carrega as recompensas do rewards.yml
        Bukkit.getConsoleSender().sendMessage("§a[FrKillsRank] Plugin ativado!");
        startScoreboardUpdater(); // Inicia o loop de atualização do Scoreboard

        // Carrega as configurações do evento do events.yml
        KillCompetitionManager.loadEventConfig();

        // Carrega os dados dos jogadores
        PlayerDataManager.loadPlayerData();

        // Carrega as missões do arquivo missions.yml
        MissionsManager.loadMissions();

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new MobKillListener(), this);
        getServer().getPluginManager().registerEvents(new RewardsGUI(), this);
        getServer().getPluginManager().registerEvents(new MainGUIListener(),this);

        // Registrar comandos já existentes
        getCommand("reloadkillsconfig").setExecutor(new CommandReloadConfig());
        getCommand("recompensas").setExecutor(new CommandRecompensas());
        getCommand("pointskills").setExecutor(new CommandPointSkills());
        getCommand("pointsrank").setExecutor(new CommandPointsRank());
        getCommand("pointsranktop").setExecutor(new CommandPointsRankTop());
        getCommand("addpoints").setExecutor(new CommandAddPoints());
        getCommand("removepoints").setExecutor(new CommandRemovePoints());
        getCommand("missions").setExecutor(new CommandMissions());

        // Registrar o comando principal (se aplicável)
        if (getCommand("frkillsrank") == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "O comando 'frkillsrank' não foi encontrado no plugin.yml!");
        } else {
            getCommand("frkillsrank").setExecutor(new CommandFrKillsRank());
            getCommand("frkillsrank").setTabCompleter(new CommandFrKillsRank());
        }

        // Inicia o evento automaticamente, se desejado:
        if (KillCompetitionManager.eventEnabled) {
            KillCompetitionManager.startEvent();

        }

    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§c[FrKillsRank] Plugin desativado!");
    }

    public static FrKillsRank getInstance() {
        return instance;
    }


    private void startScoreboardUpdater() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ScoreboardManager.updateScoreboard(player);
            }
        }, 0L, 100L); // Atualiza a cada 5 segundos (100 ticks)
    }


}
