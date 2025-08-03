package fabiofr32.frKillsRank.Items.GodsSword;

// ... (todos os seus imports)
import fabiofr32.frKillsRank.managers.ConfigManager; // <- IMPORTANTE: adicione este import se não existir
import fabiofr32.frKillsRank.FrKillsRank;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class GodsSwordListener implements Listener {

    private final FrKillsRank plugin;
    private final NamespacedKey godsSwordKey;
    private final Map<UUID, Long> witherSkullCooldowns = new HashMap<>();

    // --- Variáveis carregadas do config.yml ---
    // REMOVEMOS a variável 'customDamage' daqui para que ela não seja mais guardada
    private final int resistanceLevel;
    private final long witherSkullCooldown;
    private final double witherSkullSpeedMultiplier;
    private final boolean homingEnabled;
    private final double homingDetectionRadius;
    private final float explosionVisualPower;

    public GodsSwordListener(FrKillsRank plugin) {
        this.plugin = plugin;
        this.godsSwordKey = new NamespacedKey(plugin, "gods_sword");

        FileConfiguration config = plugin.getConfig();
        // Carrega todas as configurações
        this.resistanceLevel = config.getInt("gods_sword.resistance_level", 2);
        this.witherSkullCooldown = config.getLong("gods_sword.wither_skull.cooldown_seconds", 10);
        this.witherSkullSpeedMultiplier = config.getDouble("gods_sword.wither_skull.speed_multiplier", 1.8);
        this.homingEnabled = config.getBoolean("gods_sword.wither_skull.proximity_homing.enable", true);
        this.homingDetectionRadius = config.getDouble("gods_sword.wither_skull.proximity_homing.detection_radius", 4.0);
        this.explosionVisualPower = (float) config.getDouble("gods_sword.wither_skull.explosion_visual_power", 3.0);
        // REMOVEMOS a linha que carregava o customDamage para a variável

        // A mensagem de diagnóstico não é mais necessária aqui

        startResistanceEffectTask();
    }

    // ... (onProjectileHit e launchHomingSkull continuam os mesmos) ...

    private void dealCustomExplosion(Player shooter, Location location) {
        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 3, 1, 1, 1, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);


        double radius = this.explosionVisualPower;
        final double damage = plugin.getConfig().getDouble("gods_sword.wither_skull.custom_damage", 15.0);
        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && !entity.getUniqueId().equals(shooter.getUniqueId())) {

                // ===============================================================================
                // MUDANÇA PRINCIPAL: Agora busca o dano diretamente do ConfigManager
                // ===============================================================================
                ((LivingEntity) entity).damage(damage, shooter);
            }
        }
    }

    // --- O resto do código permanece o mesmo ---

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof WitherSkull && event.getEntity().hasMetadata("gods_sword_skull")) {
            event.setCancelled(true);
            WitherSkull skull = (WitherSkull) event.getEntity();
            dealCustomExplosion((Player) skull.getShooter(), skull.getLocation());
            skull.remove();
        }
    }

    private void launchHomingSkull(Player player) {
        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.2f);

        WitherSkull skull = player.launchProjectile(WitherSkull.class, player.getLocation().getDirection().multiply(witherSkullSpeedMultiplier));
        skull.setShooter(player);
        skull.setMetadata("gods_sword_skull", new FixedMetadataValue(plugin, true));

        if (!homingEnabled) return;

        new BukkitRunnable() {
            private int ticksLived = 0;
            @Override
            public void run() {
                if (skull.isDead() || ticksLived++ > 100) {
                    this.cancel();
                    return;
                }
                skull.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, skull.getLocation(), 1, 0, 0, 0, 0);

                for (Entity nearbyEntity : skull.getNearbyEntities(homingDetectionRadius, homingDetectionRadius, homingDetectionRadius)) {
                    if (nearbyEntity instanceof LivingEntity && !nearbyEntity.getUniqueId().equals(player.getUniqueId())) {
                        dealCustomExplosion(player, nearbyEntity.getLocation());
                        skull.remove();
                        this.cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void startResistanceEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (isGodsSword(player.getInventory().getItemInMainHand())) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, resistanceLevel - 1, false, false, true));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean isGodsSword(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(godsSwordKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isGodsSword(player.getInventory().getItemInMainHand())) return;

        Action action = event.getAction();
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
            event.setCancelled(true);
            if (checkCooldown(player)) return;
            setCooldown(player);
            launchHomingSkull(player);
        }
    }

    private boolean checkCooldown(Player player) {
        if (witherSkullCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = ((witherSkullCooldowns.get(player.getUniqueId()) / 1000) + witherSkullCooldown) - (System.currentTimeMillis() / 1000);
            if (timeLeft > 0) {
                String message = "§cEspere §e" + timeLeft + " §csegundos para usar a habilidade novamente.";
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                return true;
            }
        }
        return false;
    }

    private void setCooldown(Player player) {
        witherSkullCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}