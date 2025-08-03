package fabiofr32.frKillsRank.Items.DarkzinKatana;

import fabiofr32.frKillsRank.FrKillsRank;
// NOVO: Import necessário para verificar o status do PvP.
import fabiofr32.frKillsRank.managers.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DarkzinKatanaListener implements Listener {

    // --- CONSTANTS FOR FIREBALL ABILITY ---
    private static final long FIREBALL_COOLDOWN_MS = 8 * 1000;
    private static final double FIREBALL_DAMAGE = 24.0;
    private static final int FIREBALL_FIRE_TICKS = 100;

    // --- USABILITY CONSTANTS ---
    private static final double FIREBALL_DETONATION_RADIUS = 4.0;
    private static final double FIREBALL_SPEED_MULTIPLIER = 1.8;
    private static final int TARGET_LOCK_RANGE = 30;

    private final FrKillsRank plugin;
    private final NamespacedKey katanaKey;
    private final NamespacedKey customFireballKey;

    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> removalTasks = new HashMap<>();

    public DarkzinKatanaListener(FrKillsRank plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, "darkzin_katana");
        this.customFireballKey = new NamespacedKey(plugin, "custom_fireball");
    }

    // Prevenção de dano de lava/fogo
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();

        if ((cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.FIRE_TICK)
                && isHoldingKatana(player)) {
            event.setCancelled(true);
            player.setFireTicks(0);
        }
    }

    // Habilidade ativa: Bola de Fogo
    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().isLeftClick() || !player.isSneaking()) return;
        if (!isHoldingKatana(player)) return;

        // Cooldown check
        if (fireballCooldowns.containsKey(player.getUniqueId())) {
            long cooldownEnd = fireballCooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() < cooldownEnd) {
                long remainingSeconds = (cooldownEnd - System.currentTimeMillis()) / 1000;
                player.sendActionBar(Component.text("Bola de Fogo em recarga por " + (remainingSeconds + 1) + "s", NamedTextColor.RED));
                return;
            }
        }

        LivingEntity target = null;
        RayTraceResult rayTraceResult = player.rayTraceEntities(TARGET_LOCK_RANGE, false);
        if (rayTraceResult != null && rayTraceResult.getHitEntity() instanceof LivingEntity) {
            target = (LivingEntity) rayTraceResult.getHitEntity();
        }

        final LivingEntity finalTarget = target;

        Vector initialVelocity = player.getLocation().getDirection().multiply(FIREBALL_SPEED_MULTIPLIER);
        Fireball fireball = player.launchProjectile(Fireball.class, initialVelocity);
        fireball.getPersistentDataContainer().set(customFireballKey, PersistentDataType.BYTE, (byte) 1);
        fireball.setYield(0);
        fireball.setIsIncendiary(false);

        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (fireball.isDead() || !fireball.isValid()) {
                    this.cancel();
                    return;
                }

                if (finalTarget != null && !finalTarget.isDead()) {
                    Vector targetDirection = finalTarget.getEyeLocation().toVector().subtract(fireball.getLocation().toVector()).normalize();
                    fireball.setVelocity(targetDirection.multiply(FIREBALL_SPEED_MULTIPLIER));
                }

                fireball.getWorld().spawnParticle(Particle.FLAME, fireball.getLocation(), 5, 0.1, 0.1, 0.1, 0.02);
                fireball.getWorld().spawnParticle(Particle.SMOKE, fireball.getLocation(), 3, 0.1, 0.1, 0.01);
                if (Math.random() < 0.2) {
                    fireball.getWorld().spawnParticle(Particle.LAVA, fireball.getLocation(), 1, 0, 0, 0, 0);
                }

                Collection<Entity> nearbyEntities = fireball.getWorld().getNearbyEntities(fireball.getLocation(), FIREBALL_DETONATION_RADIUS, FIREBALL_DETONATION_RADIUS, FIREBALL_DETONATION_RADIUS);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && !entity.getUniqueId().equals(player.getUniqueId())) {
                        LivingEntity hitTarget = (LivingEntity) entity;

                        // --- NOVO: BLOCO DE VERIFICAÇÃO DE PVP ---
                        if (hitTarget instanceof Player) {
                            Player attackedPlayer = (Player) hitTarget;

                            // Verifica se o atacante tem o pvp desativado
                            if (!PlayerDataManager.isPvPEnabled(player)) {
                                player.sendMessage("§cVocê tem o PvP desativado e não pode atacar outros jogadores!");
                                fireball.remove();
                                this.cancel();
                                return; // Sai da task
                            }

                            // Verifica se o alvo tem o pvp desativado
                            if (!PlayerDataManager.isPvPEnabled(attackedPlayer)) {
                                player.sendMessage("§cEste jogador tem o PvP desativado!");
                                fireball.remove();
                                this.cancel();
                                return; // Sai da task
                            }
                        }
                        // --- FIM DO BLOCO DE VERIFICAÇÃO ---

                        // Se passou pelas verificações (ou se não for um player), causa o dano
                        hitTarget.damage(FIREBALL_DAMAGE, player);
                        hitTarget.setFireTicks(FIREBALL_FIRE_TICKS);

                        hitTarget.getWorld().spawnParticle(Particle.EXPLOSION, hitTarget.getLocation().add(0, 1, 0), 1);
                        hitTarget.getWorld().playSound(hitTarget.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);

                        fireball.remove();
                        this.cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        fireballCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + FIREBALL_COOLDOWN_MS);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        boolean isHolding = isHoldingKatana(player);
        boolean isInLava = player.getLocation().getBlock().getType() == Material.LAVA;
        boolean wasInLava = event.getFrom().getBlock().getType() == Material.LAVA;

        if (isHolding) {
            if (isInLava) {
                if (removalTasks.containsKey(playerUUID)) {
                    removalTasks.get(playerUUID).cancel();
                    removalTasks.remove(playerUUID);
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 6000, 0, false, false, true));
            } else if (wasInLava) {
                scheduleEffectRemoval(player);
            }
        } else {
            PotionEffect fireRes = player.getPotionEffect(PotionEffectType.FIRE_RESISTANCE);
            if (fireRes != null && fireRes.getDuration() > 2000) {
                scheduleEffectRemoval(player);
            }
        }
    }

    private void scheduleEffectRemoval(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (removalTasks.containsKey(playerUUID)) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isHoldingKatana(player) || player.getLocation().getBlock().getType() != Material.LAVA) {
                    player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.removePotionEffect(PotionEffectType.STRENGTH);
                    player.removePotionEffect(PotionEffectType.REGENERATION);
                    player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
                }
                removalTasks.remove(playerUUID);
            }
        };
        task.runTaskLater(plugin, 60L);
        removalTasks.put(playerUUID, task);
    }

    private boolean isHoldingKatana(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NETHERITE_SWORD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(katanaKey, PersistentDataType.BYTE);
    }
}