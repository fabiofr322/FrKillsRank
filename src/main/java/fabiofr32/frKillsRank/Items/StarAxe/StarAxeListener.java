package fabiofr32.frKillsRank.Items.StarAxe; // Crie este pacote

import fabiofr32.frKillsRank.FrKillsRank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent; // Para detectar mudança de item na mão
// import org.bukkit.event.player.PlayerMoveEvent; // Remova este import, não está sendo usado
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StarAxeListener implements Listener {

    private final FrKillsRank plugin;
    private final NamespacedKey starAxeKey;
    private final Map<UUID, Long> blindnessCooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> hasteTasks = new HashMap<>(); // Para gerenciar a Haste

    // Cooldown para a habilidade de cegueira
    private static final long BLINDNESS_COOLDOWN_MS_DEFAULT = 10 * 1000; // 10 segundos
    private static final long BLINDNESS_COOLDOWN_MS_OP = 30 * 1000; // 30 segundos (para OP)
    private static final int BLINDNESS_DURATION_TICKS = 5 * 20; // 5 segundos * 20 ticks/segundo
    private static final double BLINDNESS_RADIUS = 5.0; // Raio de cegueira

    public StarAxeListener(FrKillsRank plugin) {
        this.plugin = plugin;
        this.starAxeKey = new NamespacedKey(plugin, "star_axe");
    }

    // Método auxiliar para verificar se o jogador está segurando o Machado das Estrelas
    private boolean isHoldingStarAxe(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NETHERITE_AXE || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(starAxeKey, PersistentDataType.BYTE);
    }

    // --- Habilidade Ativa: Cegueira em Área (Shift + Clique Esquerdo) ---
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Verifica se é Shift + Clique Esquerdo
        if (!event.getAction().isRightClick() || !player.isSneaking()) {
            return;
        }

        // Verifica se está segurando o Machado das Estrelas
        if (!isHoldingStarAxe(player)) {
            return;
        }

        long cooldownDuration = BLINDNESS_COOLDOWN_MS_DEFAULT;
        // Ajusta o cooldown se o jogador for OP
        if (player.isOp()) {
            cooldownDuration = BLINDNESS_COOLDOWN_MS_OP;
        }

        // Verifica o cooldown
        if (blindnessCooldowns.containsKey(player.getUniqueId())) {
            long cooldownEnd = blindnessCooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() < cooldownEnd) {
                long remainingSeconds = (cooldownEnd - System.currentTimeMillis()) / 1000;
                player.sendActionBar(Component.text("Habilidade de Cegueira em recarga por " + (remainingSeconds + 1) + "s", NamedTextColor.RED));
                return;
            }
        }

        // Aplica a Cegueira a inimigos próximos
        Location playerLoc = player.getLocation();
        player.playSound(playerLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);

        for (LivingEntity entity : playerLoc.getNearbyLivingEntities(BLINDNESS_RADIUS)) {
            if (entity instanceof Player targetPlayer) {
                // Não cega o próprio jogador ou jogadores no modo criativo/espectador
                if (targetPlayer.equals(player) || targetPlayer.getGameMode() == GameMode.CREATIVE || targetPlayer.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
                // Opcional: só cegar se o jogador não estiver no mesmo time, etc.
            } else if (entity.equals(player)) {
                continue; // Não cega o próprio jogador se for outra entidade
            }

            // Aplica o efeito de cegueira
            entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, BLINDNESS_DURATION_TICKS, 0, false, false, true));
            // Partículas ao redor da entidade cegada
            entity.getWorld().spawnParticle(Particle.LARGE_SMOKE, entity.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.01);
        }

        // Partículas na localização do jogador para indicar a ativação
        player.getWorld().spawnParticle(Particle.PORTAL, playerLoc, 50, 0.5, 0.5, 0.5, 0.05);

        // Define o cooldown
        blindnessCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownDuration);
    }

    // --- Habilidade Passiva: Haste I ao segurar o Machado ---

    // Este evento é chamado quando o jogador troca de item na mão.
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // Cancela a tarefa de Haste existente, se houver
        if (hasteTasks.containsKey(player.getUniqueId())) {
            hasteTasks.get(player.getUniqueId()).cancel();
            hasteTasks.remove(player.getUniqueId());
        }

        // Verifica o novo item na mão
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem != null && isHoldingStarAxe(player, newItem)) {
            startHasteTask(player);
        } else {
            // Se não está mais segurando o machado ou trocou para outro item, remove o efeito
            player.removePotionEffect(PotionEffectType.HASTE);
        }
    }

    // Adicionado um método sobrecarregado para isHoldingStarAxe para ser usado no PlayerItemHeldEvent
    private boolean isHoldingStarAxe(Player player, ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_AXE || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(starAxeKey, PersistentDataType.BYTE);
    }


    // Verifica a cada tick (ou a cada x ticks) se o jogador ainda está segurando o item e aplica haste
    // Isso é mais robusto para garantir que o efeito esteja sempre ativo enquanto o item estiver na mão.
    private void startHasteTask(Player player) {
        UUID playerUUID = player.getUniqueId();
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // Se o jogador não está mais online, ou não está segurando o item, cancela a tarefa
                if (!player.isOnline() || !isHoldingStarAxe(player)) {
                    player.removePotionEffect(PotionEffectType.HASTE);
                    hasteTasks.remove(playerUUID);
                    this.cancel();
                    return;
                }
                // Aplica o efeito de Haste (duração alta para ser "permanente" enquanto segura)
                // O último 'true' faz com que o efeito seja visível
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, false, false, true)); // 40 ticks = 2 segundos
            }
        };
        // Executa a tarefa a cada 20 ticks (1 segundo)
        task.runTaskTimer(plugin, 0L, 20L);
        hasteTasks.put(playerUUID, task);
    }

    // Garante que o efeito de Haste seja removido quando o jogador sai do jogo
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (hasteTasks.containsKey(player.getUniqueId())) {
            hasteTasks.get(player.getUniqueId()).cancel();
            hasteTasks.remove(player.getUniqueId());
            player.removePotionEffect(PotionEffectType.HASTE);
        }
        // Limpar cooldowns também
        blindnessCooldowns.remove(player.getUniqueId());
    }

    // Se o jogador morrer, também remove a tarefa de haste
    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (hasteTasks.containsKey(player.getUniqueId())) {
            hasteTasks.get(player.getUniqueId()).cancel();
            hasteTasks.remove(player.getUniqueId());
            // O Spigot já remove os efeitos de poção na morte, então não precisa de .removePotionEffect aqui
        }
        // Limpar cooldowns também
        blindnessCooldowns.remove(player.getUniqueId());
    }

    // Ajusta os encantos para o Machado das Estrelas, se necessário (opcional)
    // Isso é mais para garantir que o item gerado pelo comando tenha os encantos corretos,
    // mas o ideal é que o item no shop.yml já venha com eles.
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Verifica se o jogador está segurando o machado ao entrar para iniciar a task de haste
        if (isHoldingStarAxe(player)) {
            startHasteTask(player);
        }
    }
}