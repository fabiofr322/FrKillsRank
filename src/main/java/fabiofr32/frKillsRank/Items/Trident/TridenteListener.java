package fabiofr32.frKillsRank.Items.Trident;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TridenteListener implements Listener {

    private final FrKillsRank plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownMillis = 3000;

    public TridenteListener(FrKillsRank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoAcertarComTridente(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Trident tridente)) return;
        if (!(tridente.getShooter() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!TridenteUtils.isTridenteEspecial(item, plugin)) return;

        // Aumentar dano se estiver na água
        double danoExtra = 0;
        if (player.isInWater()) danoExtra += 5;
        if (player.isSwimming()) danoExtra += 2;

        if (danoExtra > 0) {
            event.setDamage(event.getDamage() + danoExtra);

            // Efeitos visuais/sonoros
            Entity alvo = event.getEntity();
            alvo.getWorld().spawnParticle(Particle.BUBBLE_POP, alvo.getLocation(), 15, 0.3, 0.5, 0.3, 0.02);
            alvo.getWorld().playSound(alvo.getLocation(), Sound.ENTITY_DROWNED_SHOOT, 1.0f, 1.2f);
        }
    }
}
