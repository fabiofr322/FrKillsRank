package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillCompetitionManager {

    // Nova variável para o delay entre eventos
    public static int minDelayBetweenEvents;
    public static long lastEventTime = 0;

    private static File eventsFile;
    private static FileConfiguration eventsConfig;

    public static boolean eventEnabled;
    public static boolean eventActive = false;
    public static Map<UUID, Integer> eventKills = new HashMap<>();

    // Variáveis para as configurações do evento
    public static String eventName;
    public static int durationSeconds;
    public static int rewardPoints;

    // Contador para limitar o número de eventos por dia (máximo 5)
    public static int dailyEventCount = 0;

    // Converte segundos para ticks (útil para agendamentos do Bukkit)
    public static long convertSecondsToTicks(int seconds) {
        return seconds * 20L;
    }

    // Carrega as configurações do arquivo events.yml
    public static void loadEventConfig() {
        eventsFile = new File(FrKillsRank.getInstance().getDataFolder(), "events.yml");
        if (!eventsFile.exists()) {
            FrKillsRank.getInstance().saveResource("events.yml", false);
        }
        eventsConfig = YamlConfiguration.loadConfiguration(eventsFile);

        eventEnabled = eventsConfig.getBoolean("event.kills_comptetion.enabled", false);
        eventName = eventsConfig.getString("event.kills_comptetion.name", "Competição de Kills");
        durationSeconds = eventsConfig.getInt("event.kills_comptetion.duration_seconds", 600);
        rewardPoints = eventsConfig.getInt("event.kills_comptetion.reward_points", 1000);
        minDelayBetweenEvents = eventsConfig.getInt("event.kills_comptetion.min_delay_between_events", 1800);
    }

    private static int broadcastTaskId = -1; // Armazena o ID da tarefa de broadcast

    // Método para broadcast do líder atual
    public static void broadcastCurrentLeader() {
        if (!eventActive) return;

        UUID leaderUuid = null;
        int maxKills = 0;
        for (Map.Entry<UUID, Integer> entry : eventKills.entrySet()) {
            if (entry.getValue() > maxKills) {
                maxKills = entry.getValue();
                leaderUuid = entry.getKey();
            }
        }

        String message;
        if (leaderUuid != null) {
            Player leader = Bukkit.getPlayer(leaderUuid);
            message = ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.event_leader",
                            "&eEvento {event_name} ativo! Atual líder: {player} com {kills} kills."));
            message = message.replace("{event_name}", eventName)
                    .replace("{player}", leader != null ? leader.getName() : "Desconhecido")
                    .replace("{kills}", String.valueOf(maxKills));
        } else {
            message = ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.event_active",
                            "&eEvento {event_name} ativo! Ninguém registrou kills ainda."));
            message = message.replace("{event_name}", eventName);
        }

        Bukkit.broadcastMessage(message);
    }

    // Inicia o evento
    // Altere o método startEvent para agendar o broadcast
    public static void startEvent() {
        if (!eventEnabled) return;
        eventActive = true;
        eventKills.clear();

        // Mensagem de início do evento
        String startMsg = FrKillsRank.getInstance().getConfig().getString("settings.messages.event_start",
                "&aEvento {event_name} iniciado! Mate o máximo de mobs possível!");
        startMsg = ChatColor.translateAlternateColorCodes('&', startMsg.replace("{event_name}", eventName));

        Bukkit.broadcastMessage(startMsg); // Agora essa mensagem sempre será enviada

        // Agenda o fim do evento após a duração definida (convertendo segundos para ticks)
        Bukkit.getScheduler().runTaskLater(FrKillsRank.getInstance(), KillCompetitionManager::endEvent, convertSecondsToTicks(durationSeconds));

        // Agenda a tarefa de broadcast do líder a cada 60 segundos
        broadcastTaskId = Bukkit.getScheduler().runTaskTimer(FrKillsRank.getInstance(), KillCompetitionManager::broadcastCurrentLeader, 0L, convertSecondsToTicks(60)).getTaskId();
    }

    // Registra uma kill para o jogador durante o evento
    public static void recordKill(Player player) {
        if (!eventActive) return;
        UUID uuid = player.getUniqueId();
        int kills = eventKills.getOrDefault(uuid, 0);
        eventKills.put(uuid, kills + 1);
    }

    // Encerra o evento e recompensa o vencedor
    // No método endEvent, cancele a tarefa de broadcast
    public static void endEvent() {
        if (!eventActive) return;
        eventActive = false;
        // Cancele a tarefa de broadcast, se estiver agendada
        if (broadcastTaskId != -1) {
            Bukkit.getScheduler().cancelTask(broadcastTaskId);
            broadcastTaskId = -1;
        }
        UUID winnerUuid = null;
        int maxKills = 0;
        for (Map.Entry<UUID, Integer> entry : eventKills.entrySet()) {
            if (entry.getValue() > maxKills) {
                maxKills = entry.getValue();
                winnerUuid = entry.getKey();
            }
        }
        String endMsg;
        if (winnerUuid != null) {
            Player winner = Bukkit.getPlayer(winnerUuid);
            if (winner != null) {
                ConfigManager.addPoints(winner, rewardPoints);
            }
            endMsg = ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.event_end",
                            "&eEvento {event_name} encerrado! {player} venceu com {kills} kills e ganhou {reward} pontos!"));
            endMsg = endMsg.replace("{event_name}", eventName)
                    .replace("{player}", (winner != null ? winner.getName() : "Desconhecido"))
                    .replace("{kills}", String.valueOf(maxKills))
                    .replace("{reward}", String.valueOf(rewardPoints));
        } else {
            endMsg = ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.messages.event_end_no_winner",
                            "&eEvento {event_name} encerrado! Nenhum participante."));
            endMsg = endMsg.replace("{event_name}", eventName);
        }
        Bukkit.broadcastMessage(endMsg);
    }

    // Agenda tarefas para iniciar eventos aleatoriamente e resetar o contador diário
    public static void scheduleRandomEvents() {
        // Tarefa que roda a cada 10 minutos (12000 ticks)
        Bukkit.getScheduler().runTaskTimer(FrKillsRank.getInstance(), () -> {
            if (!eventEnabled) return;
            if (eventActive) return;
            if (dailyEventCount >= 5) return;

            long currentTime = System.currentTimeMillis() / 1000; // Tempo atual em segundos
            long timeSinceLastEvent = currentTime - lastEventTime;

            if (timeSinceLastEvent < minDelayBetweenEvents) {
                Bukkit.getConsoleSender().sendMessage("§6[FrKillsRank] Aguardando o delay mínimo de " + minDelayBetweenEvents + " segundos antes de iniciar um novo evento.");
                return; // Não inicia um novo evento ainda
            }

            // Chance aleatória para iniciar o evento (20% de chance)
            if (Math.random() < 0.2) {
                startEvent();
                dailyEventCount++;
                lastEventTime = System.currentTimeMillis() / 1000; // Atualiza o tempo do último evento
            }
        }, 0L, 12000L); // Verifica a cada 10 minutos
    }

}
