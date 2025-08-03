package fabiofr32.frKillsRank.Items.SonicDashSword;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GodwardenSwordKillListener implements Listener {

    private final FrKillsRank plugin;
    private final NamespacedKey godwardenKey;

    public GodwardenSwordKillListener(FrKillsRank plugin) {
        this.plugin = plugin;
        this.godwardenKey = new NamespacedKey(plugin, "godwarden_sword");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity deadEntity = event.getEntity();
        Player killer = deadEntity.getKiller();

        if (killer == null) {
            return;
        }

        ItemStack itemInHand = killer.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR || !itemInHand.hasItemMeta()) {
            return;
        }

        PersistentDataContainer container = itemInHand.getItemMeta().getPersistentDataContainer();
        if (!container.has(godwardenKey, PersistentDataType.BYTE)) {
            return;
        }

        // Se todas as verificações passaram, executa o novo efeito de partículas.
        dispararEfeitoDeAlma(deadEntity.getLocation());
    }

    /**
     * Efeito simplificado que gera apenas uma explosão de partículas de alma
     * e um som no local da morte da entidade.
     *
     * @param center Localização onde o mob morreu.
     */
    private void dispararEfeitoDeAlma(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        // Toca um som temático no local
        world.playSound(center, Sound.BLOCK_SCULK_CHARGE, 1.5f, 0.8f);

        // Gera uma explosão de partículas de alma (SCULK_SOUL)
        // Aumentei a contagem para 30 para um efeito mais dramático.
        // As partículas aparecerão em uma área de 1x1x1 ao redor do centro.
        world.spawnParticle(Particle.SCULK_SOUL, center.add(0, 0.5, 0), 30, 0.5, 0.5, 0.5, 0.02);
    }
}