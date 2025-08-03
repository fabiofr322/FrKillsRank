package fabiofr32.frKillsRank.Items.Cerejinha_sword;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class SwordCherryListener implements Listener {

    private final JavaPlugin plugin;
    private final Set<UUID> canDash = new HashSet<>();
    private final Set<UUID> dashUsed = new HashSet<>();
    private final Set<UUID> noFallDamage = new HashSet<>();
    private final Map<UUID, Set<UUID>> dashHitEntities = new HashMap<>();
    private final Map<UUID, Long> cooldownMap = new HashMap<>();
    private final long COOLDOWN_TIME = 5_000; // 5 segundos em milissegundos

    public SwordCherryListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClickWithShift(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking()) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!SwordCherryUtils.isEspecialSword(item)) return;

        // Só salta se estiver no chão!
        if (!player.isOnGround()) return;

        // COOLDOWN
        long now = System.currentTimeMillis();
        if (cooldownMap.containsKey(uuid) && now < cooldownMap.get(uuid)) {
            long seconds = (cooldownMap.get(uuid) - now) / 1000;
            player.sendMessage(ChatColor.RED + "Aguarde " + seconds + " segundos para usar novamente!");
            return;
        }

        // Pulo alto
        player.setVelocity(new Vector(0, 1.2, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.5f);
        player.spawnParticle(Particle.CHERRY_LEAVES, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.01);

        canDash.add(uuid);
        dashUsed.remove(uuid);

        // Não tomar dano de queda
        noFallDamage.add(uuid);

        // Setar cooldown
        cooldownMap.put(uuid, now + COOLDOWN_TIME);

        // Expira após 5s
        new BukkitRunnable() {
            @Override
            public void run() {
                canDash.remove(uuid);
                dashUsed.remove(uuid);
            }
        }.runTaskLater(plugin, 20 * 5);
    }

    @EventHandler
    public void onLeftClickDash(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Dash só com clique esquerdo (em ar ou bloco)
        if (!(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) return;

        if (!canDash.contains(uuid) || dashUsed.contains(uuid)) return;
        if (player.isOnGround()) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!SwordCherryUtils.isEspecialSword(item)) return;

        // Dash na direção do olhar, respeitando totalmente o ângulo
        Vector direction = player.getEyeLocation().getDirection().normalize();
        Vector dash = direction.multiply(1.8); // Ajuste a força se quiser
        player.setVelocity(dash);

        // Efeitos visuais e sonoros
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 2f);

        // Partículas seguem o player durante o dash (por 12 ticks)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 12 || player.isOnGround()) {
                    cancel();
                    return;
                }
                player.getWorld().spawnParticle(Particle.CHERRY_LEAVES, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.08);
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        // Permite dash só uma vez
        dashUsed.add(uuid);

        // Marca para não tomar dano de queda
        noFallDamage.add(uuid);

        // Limpa entidades atingidas nesse dash
        dashHitEntities.put(uuid, new HashSet<>());

        // Checa colisão para dar dano durante o dash (por 12 ticks)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 12 || player.isOnGround()) {
                    cancel();
                    return;
                }
                Set<UUID> atingidos = dashHitEntities.get(uuid);
                for (Entity nearby : player.getNearbyEntities(1.3, 1.2, 1.3)) {
                    if (!(nearby instanceof LivingEntity target)) continue;
                    if (nearby.getUniqueId().equals(uuid)) continue;
                    if (atingidos.contains(nearby.getUniqueId())) continue;

                    // Dano e efeitos
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    target.damage(3.0, player); // Dano do dash
                    target.getWorld().spawnParticle(Particle.CHERRY_LEAVES, target.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.1);
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_DRINK, 1f, 0.6f);

                    atingidos.add(nearby.getUniqueId());
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    // Remove dano de queda se foi causado por dash
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        UUID uuid = player.getUniqueId();
        if (noFallDamage.contains(uuid)) {
            event.setCancelled(true);
            noFallDamage.remove(uuid);
        }
    }

    // (Opcional) Se quiser resetar o dash ao tocar o chão, descomente o evento abaixo:
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Se caiu no chão, remove a permissão de dash e marca para não tomar dano de queda
        if (player.isOnGround()) {
            canDash.remove(uuid);
            dashUsed.remove(uuid);
            // noFallDamage.remove(uuid); // Removido apenas quando o dano é cancelado
            dashHitEntities.remove(uuid);
        }
    }
}