package fabiofr32.frKillsRank.Items.Crossbow;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BestaListener implements Listener {

    private final FrKillsRank plugin;
    private final NamespacedKey smokeTrailKey; // Nova chave para identificar a besta de fumaça

    public BestaListener(FrKillsRank plugin) {
        this.plugin = plugin;
        this.smokeTrailKey = new NamespacedKey(plugin, "besta_fumaca");
    }

    @EventHandler
    public void aoAtirar(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack besta = event.getBow();
        if (besta == null) return;

        // Verifica se é a besta de fumaça (substitua BestaUtils pelo seu método de verificação)
        if (isBestaFumaca(besta)) {
            Projectile projectile = (Projectile) event.getProjectile();
            projectile.getPersistentDataContainer().set(smokeTrailKey, PersistentDataType.BYTE, (byte) 1);
            iniciarTrilhaDeFumaca(projectile); // Inicia o efeito de trilha
        }
    }

    private boolean isBestaFumaca(ItemStack item) {
        if (item == null || item.getType() != Material.CROSSBOW) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "besta_fumaca"),
                PersistentDataType.BYTE
        );
    }

    private void iniciarTrilhaDeFumaca(Projectile projectile) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (projectile.isDead() || projectile.isOnGround()) {
                    this.cancel();
                    return;
                }

                // Partículas de fumaça ao longo do trajeto
                Location loc = projectile.getLocation();
                projectile.getWorld().spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        loc,
                        5,
                        0.1, 0.1, 0.1, 0.02
                );

                // Aplica efeitos a mobs próximos
                for (Entity entity : projectile.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player)) {
                        livingEntity.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOWNESS,
                                60,  // 3 segundos (20 ticks = 1s)
                                1    // Nível II
                        ));
                        livingEntity.addPotionEffect(new PotionEffect(
                                PotionEffectType.NAUSEA,
                                100, // 5 segundos
                                0
                        ));
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 2); // Executa a cada 2 ticks (10 vezes por segundo)
    }

    @EventHandler
    public void aoAcertar(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        // Verifica se é um projétil de fumaça
        if (projectile.getPersistentDataContainer().has(smokeTrailKey, PersistentDataType.BYTE)) {
            // Cria uma nuvem de fumaça no local do impacto
            Location hitLoc = event.getHitBlock() != null ?
                    event.getHitBlock().getLocation() :
                    projectile.getLocation();

            // Efeito visual
            hitLoc.getWorld().spawnParticle(
                    Particle.LARGE_SMOKE,
                    hitLoc,
                    60,
                    0.5, 0.5, 0.5, 0.5
            );
            hitLoc.getWorld().playSound(
                    hitLoc,
                    Sound.BLOCK_CAMPFIRE_CRACKLE,
                    1.0f,
                    0.8f
            );

            // Aplica efeitos em área maior no impacto
            for (Entity entity : hitLoc.getWorld().getNearbyEntities(hitLoc, 3.0, 3.0, 3.0)) {
                if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player)) {
                    livingEntity.addPotionEffect(new PotionEffect(
                            PotionEffectType.BLINDNESS,
                            40,  // 2 segundos
                            0
                    ));
                }
            }
        }
    }
}