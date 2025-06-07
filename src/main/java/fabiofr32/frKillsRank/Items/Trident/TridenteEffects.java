package fabiofr32.frKillsRank.Items.Trident;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TridenteEffects {

    private final FrKillsRank plugin;

    public TridenteEffects(FrKillsRank plugin) {
        this.plugin = plugin;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isInWater()) continue;

                    if (TridenteUtils.isTridenteEspecial(player.getInventory().getItemInMainHand(), plugin)) {
                        aplicarEfeitos(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void aplicarEfeitos(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 60, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, true, false));

        if (player.isSwimming()) {
            player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, player.getLocation(), 10, 0.3, 0.5, 0.3, 0.01);
        }
    }
}
