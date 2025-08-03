package fabiofr32.frKillsRank.Items.SonicDashSword;

import fabiofr32.frKillsRank.FrKillsRank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GodwardenSwordListener implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final FrKillsRank plugin;
    private final long cooldownDurationMs;
    private final NamespacedKey godwardenKey;

    public GodwardenSwordListener(FrKillsRank plugin) {
        this.plugin = plugin;
        this.godwardenKey = new NamespacedKey(plugin, "godwarden_sword");
        this.cooldownDurationMs = plugin.getConfig().getLong("godwarden_sword.cooldown_segundos", 10) * 1000;

        // Inicia a nova tarefa que verifica os buffs de sculk
        startSculkBuffChecker();
    }

    @EventHandler
    public void onPlayerShiftRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND || !player.isSneaking() || !event.getAction().toString().contains("RIGHT_CLICK")) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (isNotGodwardenSword(item)) return;

        if (cooldowns.containsKey(player.getUniqueId())) {
            long cooldownEnd = cooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() < cooldownEnd) {
                long remainingSeconds = (cooldownEnd - System.currentTimeMillis()) / 1000;
                player.sendActionBar(Component.text("Carga Sônica em recarga por " + (remainingSeconds + 1) + "s", NamedTextColor.RED));
                return;
            }
        }

        lancarOndaSonica(player);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + this.cooldownDurationMs);
    }

    private void lancarOndaSonica(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1f);

        final double damage = plugin.getConfig().getDouble("godwarden_sword.dano_habilidade", 12.0);
        final int maxDistance = plugin.getConfig().getInt("godwarden_sword.distancia_maxima", 20);
        final ConfigurationSection effectsSection = plugin.getConfig().getConfigurationSection("godwarden_sword.efeitos");

        new BukkitRunnable() {
            private final Vector direction = player.getEyeLocation().getDirection();
            private final Location currentLocation = player.getEyeLocation().add(direction);
            private int distanceTraveled = 0;

            @Override
            public void run() {
                if (distanceTraveled++ >= maxDistance || currentLocation.getBlock().isSolid()) {
                    cancel();
                    return;
                }
                currentLocation.getWorld().spawnParticle(Particle.SONIC_BOOM, currentLocation, 1, 0, 0, 0, 0);
                for (Entity entity : currentLocation.getWorld().getNearbyEntities(currentLocation, 1.2, 1.2, 1.2)) {
                    if (entity instanceof LivingEntity target && !entity.equals(player)) {
                        target.damage(damage, player);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WARDEN_HURT, 1.2f, 0.8f);
                        if (effectsSection != null) {
                            if (effectsSection.getBoolean("lentidao.enable", true)) {
                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, effectsSection.getInt("lentidao.duracao_segundos", 3) * 20, effectsSection.getInt("lentidao.forca", 1)));
                            }
                            if (effectsSection.getBoolean("escuridao.enable", true)) {
                                target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, effectsSection.getInt("escuridao.duracao_segundos", 4) * 20, effectsSection.getInt("escuridao.forca", 0)));
                            }
                        }
                        cancel();
                        return;
                    }
                }
                currentLocation.add(direction);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Inicia a tarefa que verifica continuamente os jogadores para o buff de sculk.
     */
    private void startSculkBuffChecker() {
        if (!plugin.getConfig().getBoolean("godwarden_sword.sculk_buff.enable", true)) {
            return; // Não inicia a tarefa se estiver desabilitada no config
        }

        // Pega os valores do config
        final int speedAmplifier = plugin.getConfig().getInt("godwarden_sword.sculk_buff.velocidade_forca", 1);
        final int strengthAmplifier = plugin.getConfig().getInt("godwarden_sword.sculk_buff.forca_forca", 0);
        // Converte a duração de minutos para ticks (minutos * 60 segundos * 20 ticks)
        final int buffDurationTicks = plugin.getConfig().getInt("godwarden_sword.sculk_buff.duracao_minutos", 3) * 60 * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();

                    // A condição para ATIVAR ou REATIVAR o buff
                    if (!isNotGodwardenSword(itemInHand) && isStandingOnSculk(player)) {

                        // Aplica os buffs com a duração total de 3 minutos.
                        // Se o jogador já tiver o efeito, esta chamada irá simplesmente resetar a duração.
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, buffDurationTicks, speedAmplifier, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, buffDurationTicks, strengthAmplifier, false, false));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Roda a cada 20 ticks (1 segundo)
    }

    /**
     * Verifica se o jogador está em cima de qualquer tipo de bloco de sculk.
     */
    private boolean isStandingOnSculk(Player player) {
        Material blockType = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        // Checa se o nome do material contém "SCULK" (funciona para SCULK, SCULK_VEIN, SCULK_SENSOR, etc.)
        return blockType.toString().contains("SCULK");
    }

    /**
     * Função auxiliar para verificar se um item é a Godwarden Sword.
     */
    private boolean isNotGodwardenSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return true;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return !container.has(godwardenKey, PersistentDataType.BYTE);
    }
}