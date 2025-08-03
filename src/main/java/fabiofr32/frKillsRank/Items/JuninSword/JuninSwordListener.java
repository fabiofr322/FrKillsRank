package fabiofr32.frKillsRank.Items.JuninSword;

import fabiofr32.frKillsRank.FrKillsRank;
import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JuninSwordListener implements Listener {

    private final FrKillsRank plugin;
    private final NamespacedKey juninSwordKey;
    private final Map<UUID, BukkitRunnable> swordEffectTasks = new HashMap<>();

    private static final AttributeModifier SLOW_ATTACK_MODIFIER = new AttributeModifier(
            UUID.fromString("8741364d-91b6-4b20-8e10-3e28c77e6f66"),
            "junin_sword_slow_attack",
            -0.6,
            Operation.MULTIPLY_SCALAR_1
    );

    public JuninSwordListener(FrKillsRank plugin) {
        this.plugin = plugin;
        this.juninSwordKey = new NamespacedKey(plugin, "junin_sword");
    }

    private boolean isHoldingJuninSword(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NETHERITE_SWORD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(juninSwordKey, PersistentDataType.BYTE);
    }

    private boolean isHoldingJuninSword(Player player, ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(juninSwordKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
        if (isHoldingJuninSword(player, previousItem)) {
            if (swordEffectTasks.containsKey(playerUUID)) {
                swordEffectTasks.get(playerUUID).cancel();
                swordEffectTasks.remove(playerUUID);
            }
            removeAttackSpeedModifier(previousItem);
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.removePotionEffect(PotionEffectType.SPEED);
        }

        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (isHoldingJuninSword(player, newItem)) {
            startJuninSwordEffectsTask(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null || event.getFrom().getWorld().equals(to.getWorld())) {
            return;
        }

        Player player = event.getPlayer();
        if (isHoldingJuninSword(player)) {
            if (swordEffectTasks.containsKey(player.getUniqueId())) {
                swordEffectTasks.get(player.getUniqueId()).cancel();
            }
            startJuninSwordEffectsTask(player);
        }
    }

    private void startJuninSwordEffectsTask(Player player) {
        UUID playerUUID = player.getUniqueId();
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isHoldingJuninSword(player)) {
                    cancel();
                    return;
                }

                ItemStack sword = player.getInventory().getItemInMainHand();
                if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 2, false, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false, true));
                    removeAttackSpeedModifier(sword);
                } else {
                    player.removePotionEffect(PotionEffectType.STRENGTH);
                    player.removePotionEffect(PotionEffectType.SPEED);
                    applyAttackSpeedModifier(sword);
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
        swordEffectTasks.put(playerUUID, task);
    }

    private void applyAttackSpeedModifier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!isHoldingJuninSword(null, item)) return;

        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
        // **CORREÇÃO AQUI:** Usando o nome correto do atributo.
        if (modifiers == null || !modifiers.containsEntry(Attribute.ATTACK_SPEED, SLOW_ATTACK_MODIFIER)) {
            meta.addAttributeModifier(Attribute.ATTACK_SPEED, SLOW_ATTACK_MODIFIER);
            item.setItemMeta(meta);
        }
    }

    private void removeAttackSpeedModifier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!isHoldingJuninSword(null, item)) return;

        // **CORREÇÃO AQUI:** Usando o nome correto do atributo.
        if (meta.removeAttributeModifier(Attribute.ATTACK_SPEED, SLOW_ATTACK_MODIFIER)) {
            item.setItemMeta(meta);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        if (isHoldingJuninSword(player)) {
            // **MELHORIA AQUI:** Modificando o dano base para melhor compatibilidade.
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, event.getDamage(EntityDamageEvent.DamageModifier.BASE) + 5.0);

            player.getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.01);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        cleanup(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        cleanup(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isHoldingJuninSword(player)) {
                    startJuninSwordEffectsTask(player);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private void cleanup(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (swordEffectTasks.containsKey(playerUUID)) {
            swordEffectTasks.get(playerUUID).cancel();
            swordEffectTasks.remove(playerUUID);
        }
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);
        if (player.getInventory().getItemInMainHand() != null) {
            removeAttackSpeedModifier(player.getInventory().getItemInMainHand());
        }
    }
}