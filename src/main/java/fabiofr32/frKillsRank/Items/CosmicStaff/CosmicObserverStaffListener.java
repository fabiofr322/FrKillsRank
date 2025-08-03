package fabiofr32.frKillsRank.Items.CosmicStaff;

import com.google.common.collect.Multimap;
import fabiofr32.frKillsRank.FrKillsRank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CosmicObserverStaffListener implements Listener {

    private final FrKillsRank plugin;
    private final NamespacedKey staffKey;
    private final NamespacedKey vulnerabilityKey;
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private final Map<UUID, Long> stunCooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> passiveEffectTasks = new HashMap<>();
    private final Map<UUID, Long> ultimateCooldowns = new HashMap<>();

    // Constantes do Cajado (mantidas da sua versão)
    private static final long TELEPORT_COOLDOWN_MS = 3 * 1000;
    private static final double TELEPORT_DISTANCE = 16.0;
    private static final long STUN_COOLDOWN_MS = 2 * 1000;
    private static final int STUN_DURATION_TICKS = 6 * 20;
    private static final double STUN_RADIUS = 7.0;
    private static final int LEVITATION_DURATION_TICKS = 5 * 20;
    private static final double COSMIC_DAMAGE = 10.0;
    private static final int VULNERABILITY_DURATION_TICKS = 5 * 20;

    // Constantes da Habilidade Suprema (mantidas da sua versão)
    private static final long ULTIMATE_COOLDOWN_MS = 120 * 1000;
    private static final int ULTIMATE_DURATION_TICKS = 240 * 20;
    private static final double ULTIMATE_RADIUS = 15.0;

    private static final AttributeModifier SLOW_FALL_MODIFIER = new AttributeModifier(
            UUID.fromString("6d7f8d9e-1f2a-4c3b-9e0a-5f6b7c8d9e0f"),
            "cosmic_staff_slow_fall",
            -0.06,
            Operation.ADD_NUMBER
    );

    public CosmicObserverStaffListener(FrKillsRank plugin) {
        this.plugin = plugin;
        this.staffKey = new NamespacedKey(plugin, "cosmic_observer_staff");
        this.vulnerabilityKey = new NamespacedKey(plugin, "cosmic_vulnerability");
    }

    private boolean isHoldingCosmicStaff(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_ROD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(staffKey, PersistentDataType.BYTE);
    }

    private boolean isHoldingCosmicStaff(Player player, ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(staffKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isHoldingCosmicStaff(player)) return;

        Action action = event.getAction();
        boolean isSneaking = player.isSneaking();

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && isSneaking) {
            handleDimensionalLeap(event);
        } else if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
            if (isSneaking) {
                handleEventHorizon(event);
            } else {
                handleCosmicPulse(event);
            }
        }
    }

    private void handleDimensionalLeap(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);

        if (teleportCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() < teleportCooldowns.get(player.getUniqueId())) {
            long remainingSeconds = (teleportCooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            player.sendActionBar(Component.text("Salto Dimensional em recarga por " + (remainingSeconds + 1) + "s", NamedTextColor.YELLOW));
            return;
        }

        Vector direction = player.getLocation().getDirection().normalize().multiply(TELEPORT_DISTANCE);
        Location targetLoc = player.getLocation().add(direction);
        Location safeLoc = findSafeTeleportLocation(targetLoc);

        if (safeLoc != null) {
            player.teleport(safeLoc);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, safeLoc, 30, 0.5, 0.5, 0.5, 0.1);
            teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + TELEPORT_COOLDOWN_MS);
        } else {
            player.sendActionBar(Component.text("Não foi possível encontrar um local seguro para teletransportar.", NamedTextColor.RED));
        }
    }

    private Location findSafeTeleportLocation(Location loc) {
        for (int i = 0; i < 3; i++) {
            if (isSafe(loc.clone().add(0, i, 0))) return loc.clone().add(0, i, 0);
        }
        for (int i = -1; i > -3; i--) {
            if (isSafe(loc.clone().add(0, i, 0))) return loc.clone().add(0, i, 0);
        }
        return null;
    }

    private boolean isSafe(Location loc) {
        return loc.getBlock().isPassable() && loc.clone().add(0, 1, 0).getBlock().isPassable();
    }

    private void handleCosmicPulse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);

        if (stunCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() < stunCooldowns.get(player.getUniqueId())) {
            long remainingSeconds = (stunCooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            player.sendActionBar(Component.text("Pulso Cósmico em recarga por " + (remainingSeconds + 1) + "s", NamedTextColor.YELLOW));
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.2f);
        LivingEntity target = findTarget(player);
        new HomingProjectileTask(player, target).runTaskTimer(plugin, 0L, 1L);
        stunCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + STUN_COOLDOWN_MS);
    }

    private LivingEntity findTarget(Player player) {
        final double range = 30.0;
        final double maxAngle = 15.0;
        Vector playerDirection = player.getEyeLocation().getDirection();

        return player.getNearbyEntities(range, range, range).stream()
                .filter(e -> e instanceof LivingEntity && !e.equals(player) && player.hasLineOfSight(e))
                .map(e -> (LivingEntity) e)
                .filter(e -> {
                    Vector toEntity = e.getEyeLocation().toVector().subtract(player.getEyeLocation().toVector());
                    return toEntity.angle(playerDirection) < Math.toRadians(maxAngle);
                })
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())))
                .orElse(null);
    }

    private class HomingProjectileTask extends BukkitRunnable {
        private final Player caster;
        private final LivingEntity target;
        private Location currentLocation;
        private Vector currentVelocity;
        private int lifeTicks = 0;

        public HomingProjectileTask(Player caster, LivingEntity target) {
            this.caster = caster;
            this.target = target;
            this.currentLocation = caster.getEyeLocation().clone();
            this.currentVelocity = caster.getEyeLocation().getDirection().multiply(0.8);
        }

        @Override
        public void run() {
            caster.getWorld().spawnParticle(Particle.REVERSE_PORTAL, currentLocation, 10, 0.2, 0.2, 0.2, 0.5);

            if (target != null && !target.isDead() && target.isValid()) {
                Vector perfectDirection = target.getEyeLocation().toVector().subtract(currentLocation.toVector()).normalize();
                this.currentVelocity = this.currentVelocity.multiply(0.2).add(perfectDirection.multiply(0.8)).normalize().multiply(0.8);
            }

            currentLocation.add(currentVelocity);

            if (!currentLocation.getBlock().isPassable() || lifeTicks > 100) {
                detonatePulse(currentLocation, caster);
                this.cancel();
                return;
            }

            Collection<Entity> nearby = caster.getWorld().getNearbyEntities(currentLocation, 1.5, 1.5, 1.5);
            boolean hasHit = false;
            for (Entity entity : nearby) {
                if (entity instanceof LivingEntity && !entity.equals(caster)) {
                    hasHit = true;
                    break;
                }
            }

            if (hasHit) {
                detonatePulse(currentLocation, caster);
                this.cancel();
            }
            lifeTicks++;
        }
    }

    private void detonatePulse(Location center, Player caster) {
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);
        center.getWorld().playSound(center, Sound.ITEM_SHIELD_BREAK, 1.0f, 0.8f);

        for (int i = 0; i < 60; i++) {
            double angle = 2 * Math.PI * Math.random();
            double radius = STUN_RADIUS * Math.random();
            Location particleLoc = center.clone().add(radius * Math.cos(angle), 0.5 + Math.random() * 1.5, radius * Math.sin(angle));
            caster.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 1, 0, 0, 0, 0);
            caster.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 1, 0.5, 0.5, 0.5, 0);
            if (Math.random() < 0.2) caster.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 1);
        }

        for (LivingEntity entity : center.getWorld().getNearbyEntities(center, STUN_RADIUS, STUN_RADIUS, STUN_RADIUS).stream().filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).collect(Collectors.toList())) {
            if (entity.equals(caster) || (entity instanceof Player p && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR))) continue;
            entity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, STUN_DURATION_TICKS, 4));
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, STUN_DURATION_TICKS, 2));
            entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, LEVITATION_DURATION_TICKS, 0));
            entity.damage(COSMIC_DAMAGE, caster);
            entity.getPersistentDataContainer().set(vulnerabilityKey, PersistentDataType.LONG, System.currentTimeMillis() + (long) VULNERABILITY_DURATION_TICKS * 50);
            entity.getWorld().spawnParticle(Particle.CRIT, entity.getEyeLocation(), 150, 0.3, 0.4, 0.3, 0.1);
        }
    }

    private void handleEventHorizon(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);

        if (ultimateCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() < ultimateCooldowns.get(player.getUniqueId())) {
            long remainingMillis = ultimateCooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
            long remainingSeconds = remainingMillis / 1000;
            player.sendActionBar(Component.text("Horizonte de Eventos em recarga por " + (remainingSeconds / 60) + "m " + (remainingSeconds % 60) + "s", NamedTextColor.RED, TextDecoration.BOLD));
            return;
        }

        player.setInvulnerable(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 10, false, false));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);

        new BukkitRunnable() {
            int ticks = 0;
            public void run() {
                if (ticks >= 30) {
                    player.setInvulnerable(false);
                    createEventHorizon(player);
                    ultimateCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + ULTIMATE_COOLDOWN_MS);
                    this.cancel();
                    return;
                }
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 20, 1, 1, 1, 0.1);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void createEventHorizon(Player caster) {
        Location center = caster.getLocation();
        caster.getWorld().playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 0.5f);
        caster.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);

        new BukkitRunnable() {
            int duration = ULTIMATE_DURATION_TICKS;
            public void run() {
                if (duration <= 0) {
                    this.cancel();
                    return;
                }
                for (double i = 0; i <= Math.PI; i += Math.PI / 12) {
                    double radius = Math.sin(i) * ULTIMATE_RADIUS;
                    double y = Math.cos(i) * ULTIMATE_RADIUS;
                    for (double j = 0; j < Math.PI * 2; j += Math.PI / 12) {
                        center.getWorld().spawnParticle(Particle.ENCHANT, center.clone().add(Math.cos(j) * radius, y, Math.sin(j) * radius), 1, 0, 0, 0, 0);
                    }
                }
                for (Entity entity : center.getWorld().getNearbyEntities(center, ULTIMATE_RADIUS, ULTIMATE_RADIUS, ULTIMATE_RADIUS)) {
                    if (entity instanceof LivingEntity le) {
                        if (le.getUniqueId().equals(caster.getUniqueId())) {
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0));
                            le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1));
                        } else {
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3));
                        }
                    } else if (entity instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player && !proj.getShooter().equals(caster)) {
                        proj.setVelocity(proj.getVelocity().multiply(0.5));
                    }
                }
                duration -= 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (passiveEffectTasks.containsKey(player.getUniqueId())) {
            passiveEffectTasks.get(player.getUniqueId()).cancel();
            passiveEffectTasks.remove(player.getUniqueId());
            ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
            if (previousItem != null) removeGravityModifier(previousItem);
        }
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (isHoldingCosmicStaff(player, newItem)) {
            startCosmicStaffPassiveEffectsTask(player);
        }
    }

    private void startCosmicStaffPassiveEffectsTask(Player player) {
        UUID playerUUID = player.getUniqueId();
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isHoldingCosmicStaff(player)) {
                    removeGravityModifier(player.getInventory().getItemInMainHand());
                    passiveEffectTasks.remove(playerUUID);
                    this.cancel();
                    return;
                }
                applyGravityModifier(player.getInventory().getItemInMainHand());
                RayTraceResult result = player.rayTraceEntities(30, true);
                if (result != null && result.getHitEntity() instanceof LivingEntity target) {
                    double currentHealth = Math.round(target.getHealth() * 10.0) / 10.0;
                    double maxHealth = Math.round(target.getAttribute(Attribute.MAX_HEALTH).getValue() * 10.0) / 10.0;
                    String effects = target.getActivePotionEffects().stream().map(p -> p.getType().getName().toLowerCase() + " " + (p.getAmplifier() + 1)).collect(Collectors.joining(", "));
                    Component healthComp = Component.text(" ❤ " + currentHealth + " / " + maxHealth, NamedTextColor.RED);
                    Component effectsComp = effects.isEmpty() ? Component.empty() : Component.text(" | Efeitos: " + effects, NamedTextColor.AQUA);
                    player.sendActionBar(Component.text(target.getName(), NamedTextColor.WHITE, TextDecoration.BOLD).append(healthComp).append(effectsComp));
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 10L);
        passiveEffectTasks.put(playerUUID, task);
    }

    private void applyGravityModifier(ItemStack item) {
        if (item == null || !isHoldingCosmicStaff(null, item)) return;
        ItemMeta meta = item.getItemMeta();
        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
        if (modifiers == null || !modifiers.containsEntry(Attribute.GRAVITY, SLOW_FALL_MODIFIER)) {
            meta.addAttributeModifier(Attribute.GRAVITY, SLOW_FALL_MODIFIER);
            item.setItemMeta(meta);
        }
    }

    private void removeGravityModifier(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(staffKey, PersistentDataType.BYTE)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta.removeAttributeModifier(Attribute.GRAVITY, SLOW_FALL_MODIFIER)) {
            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        cancelAllCleanup(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        cancelAllCleanup(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isHoldingCosmicStaff(event.getPlayer())) {
                    startCosmicStaffPassiveEffectsTask(event.getPlayer());
                }
            }
        }.runTaskLater(plugin, 5L);
    }

    private void cancelAllCleanup(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (passiveEffectTasks.containsKey(playerUUID)) {
            passiveEffectTasks.get(playerUUID).cancel();
            passiveEffectTasks.remove(playerUUID);
        }
        teleportCooldowns.remove(playerUUID);
        stunCooldowns.remove(playerUUID);
        ultimateCooldowns.remove(playerUUID);
        removeGravityModifier(player.getInventory().getItemInMainHand());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerFallDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getCause() != org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        if (isHoldingCosmicStaff(player)) {
            event.setCancelled(true);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.5, 0.1, 0.5, 0.01);
        }
    }
}