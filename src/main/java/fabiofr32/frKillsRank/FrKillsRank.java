package fabiofr32.frKillsRank;

import fabiofr32.frKillsRank.commands.*;
import fabiofr32.frKillsRank.events.MobKillListener;
import fabiofr32.frKillsRank.gui.*;
import fabiofr32.frKillsRank.listeners.ChatListener;
import fabiofr32.frKillsRank.listeners.PlayerDeathListener;
import fabiofr32.frKillsRank.listeners.PvPListener;
import fabiofr32.frKillsRank.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrKillsRank extends JavaPlugin {

    private static FrKillsRank instance;
    private MythicMobsIntegrationManager mythicMobsIntegrationManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        RewardsManager.loadRewards(); // Carrega as recompensas do rewards.yml
        Bukkit.getConsoleSender().sendMessage("§a[FrKillsRank] Plugin ativado!");
        startScoreboardUpdater(); // Inicia o loop de atualização do Scoreboard
        startTagUpdate();

        mythicMobsIntegrationManager = new MythicMobsIntegrationManager(this);
        mythicMobsIntegrationManager.checkAndNotify();

        ShopManager.loadShopConfig();
        KillCompetitionManager.loadEventConfig();
        PlayerDataManager.loadPlayerData();
        MissionsManager.loadMissions();

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new MobKillListener(), this);
        getServer().getPluginManager().registerEvents(new RewardsGUI(), this);
        getServer().getPluginManager().registerEvents(new MainGUIListener(), this);
        getServer().getPluginManager().registerEvents(new ShopGUIListener(), this);
        getServer().getPluginManager().registerEvents(new ConsumablesGUI(), this);
        getServer().getPluginManager().registerEvents(new ArmorsGUI(), this);
        getServer().getPluginManager().registerEvents(new SwordsGUI(), this);
        getServer().getPluginManager().registerEvents(new SupportItemsGUI(), this);
        getServer().getPluginManager().registerEvents(new LegendaryShopGUI(), this);
        getServer().getPluginManager().registerEvents(new MasterShopGUI(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new PvPListener(), this);

        getCommand("reloadkillsconfig").setExecutor(new CommandReloadConfig());
        //getCommand("recompensas").setExecutor(new CommandRecompensas());
        getCommand("pointskills").setExecutor(new CommandPointSkills());
        getCommand("pointsranktop").setExecutor(new CommandPointsRankTop());
        getCommand("addpoints").setExecutor(new CommandAddPoints());
        getCommand("removepoints").setExecutor(new CommandRemovePoints());
        getCommand("missions").setExecutor(new CommandMissions());
        getCommand("frloja").setExecutor(new CommandShop());
        getCommand("implementmm").setExecutor(new MythicMobsCommand(this));
        getCommand("pvp").setExecutor(new CommandPvP());
        getCommand("pvplist").setExecutor(new CommandPvPList());
        getCommand("mobslist").setExecutor(new MobsListCommand());


        // Registrar o comando principal (se aplicável)
        if (getCommand("frkillsrank") == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "O comando 'frkillsrank' não foi encontrado no plugin.yml!");
        } else {
            getCommand("frkillsrank").setExecutor(new CommandFrKillsRank());
            getCommand("frkillsrank").setTabCompleter(new CommandFrKillsRank());
        }

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
                if (player.isOnline()) {
                    ScoreboardManager.updateScoreboard(player);
                }
            }
        }, 0L, 20l);
    }

    private void startTagUpdate() {
        // Agendador para atualizar todas as tags a cada 10 segundos (200 ticks)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            TagManager.updateAllTags();
            TopRankManager.checkAndBroadcastTopRank();
        }, 0L, 1l);
    }
}