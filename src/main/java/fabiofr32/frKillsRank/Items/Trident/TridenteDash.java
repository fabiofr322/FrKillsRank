package fabiofr32.frKillsRank.Items.Trident;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TridenteDash implements Listener {

    private final FrKillsRank plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldown = 5000;

    public TridenteDash(FrKillsRank plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void aoUsar(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!TridenteUtils.isTridenteEspecial(player.getInventory().getItemInMainHand(), plugin)) return;
        if (!player.isInWater()) return;

        UUID id = player.getUniqueId();
        long last = cooldowns.getOrDefault(id, 0L);
        if (System.currentTimeMillis() - last < cooldown) return;

        cooldowns.put(id, System.currentTimeMillis());
        Vector impulso = player.getLocation().getDirection().normalize().multiply(1.8);
        impulso.setY(0.4);
        player.setVelocity(impulso);

        player.getWorld().spawnParticle(org.bukkit.Particle.SPLASH, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_DOLPHIN_SWIM, 1.0f, 1.5f);
    }
}

