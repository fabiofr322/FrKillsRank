package fabiofr32.frKillsRank.Items.Cerejinha_pickaxe;

import fabiofr32.frKillsRank.FrKillsRank; // Você precisará do seu plugin principal para o scheduler
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CerejinhaPickaxeListener implements Listener {

    private final FrKillsRank plugin; // Precisamos da instância principal para agendar tarefas

    // Mapas para gerenciar o estado dos jogadores
    private final Map<UUID, Long> activePlayers = new HashMap<>();    // Jogadores com a habilidade ativa
    private final Map<UUID, Long> cooldownPlayers = new HashMap<>(); // Jogadores em cooldown

    // Constantes para fácil configuração
    private static final long ABILITY_DURATION_MS = 10 * 1000; // 10 segundos de duração
    private static final long COOLDOWN_DURATION_MS = 30 * 1000; // 30 segundos de cooldown

    // Construtor para injetar a instância do plugin
    public CerejinhaPickaxeListener(FrKillsRank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoQuebrarBloco(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        UUID playerUUID = player.getUniqueId();

        // Verificação básica do item
        if (item.getType() != Material.NETHERITE_PICKAXE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData() || meta.getCustomModelData() != 1001) return;

        // --- LÓGICA DE ATIVAÇÃO, USO E COOLDOWN ---

        // 1. VERIFICA SE A HABILIDADE JÁ ESTÁ ATIVA
        if (activePlayers.containsKey(playerUUID)) {
            if (System.currentTimeMillis() < activePlayers.get(playerUUID)) {
                useAbility(event);
            } else {
                activePlayers.remove(playerUUID);
                startCooldown(player);
            }
            return;
        }

        // 2. VERIFICA SE O JOGADOR ESTÁ EM COOLDOWN
        if (cooldownPlayers.containsKey(playerUUID)) {
            long cooldownEnd = cooldownPlayers.get(playerUUID);
            if (System.currentTimeMillis() < cooldownEnd) {
                // --- ALTERAÇÃO APLICADA AQUI ---
                // Só mostra a mensagem de recarga se o jogador estiver agachado
                if (player.isSneaking()) {
                    long remainingSeconds = (cooldownEnd - System.currentTimeMillis()) / 1000;
                    player.sendActionBar(Component.text("Habilidade em recarga por mais " + (remainingSeconds + 1) + "s", NamedTextColor.RED));
                    // Mesmo mostrando a mensagem, não cancelamos o evento. O bloco quebra normalmente.
                }
                // Se não estiver agachado, simplesmente não faz nada e o bloco quebra.
                return; // Importante: sai do método para não tentar ativar a habilidade.
            } else {
                // Cooldown acabou, remove da lista e informa o jogador.
                cooldownPlayers.remove(playerUUID);
                player.sendActionBar(Component.text("Habilidade pronta!", NamedTextColor.GREEN));
                // Deixa o código continuar, pois o jogador pode estar agachado para reativar imediatamente.
            }
        }

        // 3. SE NÃO ESTÁ ATIVO NEM EM COOLDOWN, TENTA ATIVAR
        if (player.isSneaking()) {
            activateAbility(player, event);
        }
        // Se não estiver agachado, o bloco quebra normalmente, sem usar a habilidade.
    }

    private void activateAbility(Player player, BlockBreakEvent event) {
        UUID playerUUID = player.getUniqueId();
        long activeUntil = System.currentTimeMillis() + ABILITY_DURATION_MS;
        activePlayers.put(playerUUID, activeUntil);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        useAbility(event); // Usa a habilidade na primeira quebra

        // Inicia a tarefa de contagem regressiva
        new BukkitRunnable() {
            @Override
            public void run() {
                // Verifica se o jogador ainda está com a habilidade ativa
                if (!activePlayers.containsKey(playerUUID)) {
                    this.cancel();
                    return;
                }

                long remainingTime = activePlayers.get(playerUUID) - System.currentTimeMillis();

                if (remainingTime > 0) {
                    long remainingSeconds = remainingTime / 1000;
                    player.sendActionBar(Component.text("Bênção da Cerejeira: ", NamedTextColor.LIGHT_PURPLE)
                            .append(Component.text((remainingSeconds + 1) + "s", NamedTextColor.WHITE)));
                } else {
                    // Tempo acabou, remove o jogador e inicia o cooldown
                    activePlayers.remove(playerUUID);
                    startCooldown(player);
                    this.cancel(); // Para a tarefa
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Executa a cada segundo (20 ticks)
    }

    private void startCooldown(Player player) {
        cooldownPlayers.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_DURATION_MS);
        player.sendActionBar(Component.text("A Bênção da Cerejeira acabou! Aguarde para usar novamente.", NamedTextColor.GRAY));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    private void useAbility(BlockBreakEvent event) {
        event.setCancelled(true);
        BlockFace facing = getFacingDirection(event.getPlayer());
        quebrarArea3x3(event.getBlock(), event.getPlayer(), facing);
    }

    // Função que quebra a área 3x3 com base na direção
// Função que quebra a área 3x3 com base na direção
    private void quebrarArea3x3(Block centro, Player player, BlockFace facing) {
        World mundo = centro.getWorld();
        if (mundo == null) return;

        // Pega a ferramenta uma vez, fora do loop, para eficiência
        ItemStack tool = player.getInventory().getItemInMainHand();
        int blocksBroken = 0; // Contador para saber se algum bloco foi realmente quebrado

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Block blocoAlvo;
                if (facing == BlockFace.UP || facing == BlockFace.DOWN) {
                    blocoAlvo = centro.getRelative(i, 0, j);
                } else {
                    if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
                        blocoAlvo = centro.getRelative(i, j, 0);
                    } else { // EAST ou WEST
                        blocoAlvo = centro.getRelative(0, j, i);
                    }
                }

                // --- VERIFICAÇÃO DE QUEBRABILIDADE APLICADA AQUI ---
                // 1. Não tenta quebrar ar.
                // 2. Usa a API do Paper para verificar se o bloco é quebravél com a ferramenta.
                //    getDestroySpeed() retorna 0.0f para blocos inquebráveis como Bedrock.
                if (!blocoAlvo.getType().isAir() && blocoAlvo.getDestroySpeed(tool) > 0.0f) {
                    // Quebra o bloco naturalmente, o que também vai respeitar proteções de área de outros plugins.
                    if (blocoAlvo.breakNaturally(tool)) {
                        blocksBroken++; // Incrementa o contador se a quebra for bem-sucedida
                    }
                }
            }
        }

        // --- EFEITOS VISUAIS SÓ OCORREM SE ALGO FOI QUEBRADO ---
        if (blocksBroken > 0) {
            Location centroLoc = centro.getLocation().add(0.5, 0.5, 0.5);
            mundo.spawnParticle(Particle.CHERRY_LEAVES, centroLoc, 40, 1.2, 1.2, 1.2, 0.01);
            mundo.playSound(centroLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.5f);
            // A função de desenhar contorno foi removida no seu código, mantive assim.
            // Se quiser de volta, adicione a chamada aqui: desenharContornoArea(centro, facing, mundo);
        }
    }

    // Função para desenhar apenas o contorno de uma área 3x3
    private void desenharContornoArea(Block centro, BlockFace facing, World mundo) {
        // ... (seu código aqui)
        Particle particle = Particle.END_ROD;
        double step = 0.25;
        double startX = centro.getX() - 1;
        double startY = centro.getY() - 1;
        double startZ = centro.getZ() - 1;

        if (facing == BlockFace.UP || facing == BlockFace.DOWN) {
            double y = (facing == BlockFace.UP) ? centro.getY() + 2 : centro.getY() - 1;
            for (double d = 0; d <= 3; d += step) {
                mundo.spawnParticle(particle, startX + d, y, startZ, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, startX + d, y, startZ + 3, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, startX, y, startZ + d, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, startX + 3, y, startZ + d, 1, 0, 0, 0, 0);
            }
        } else if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            double z = (facing == BlockFace.SOUTH) ? centro.getZ() + 2 : centro.getZ() - 1;
            for (double d = 0; d <= 3; d += step) {
                mundo.spawnParticle(particle, startX + d, startY, z, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, startX + d, startY + 3, z, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, startX, startY + d, z, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, startX + 3, startY + d, z, 1, 0, 0, 0, 0);
            }
        } else {
            double x = (facing == BlockFace.EAST) ? centro.getX() + 2 : centro.getX() - 1;
            for (double d = 0; d <= 3; d += step) {
                mundo.spawnParticle(particle, x, startY, startZ + d, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, x, startY + 3, startZ + d, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, x, startY + d, startZ, 1, 0, 0, 0, 0);
                mundo.spawnParticle(particle, x, startY + d, startZ + 3, 1, 0, 0, 0, 0);
            }
        }
    }

    // Função auxiliar para obter a direção principal que o jogador está olhando
    private BlockFace getFacingDirection(Player player) {
        // ... (seu código aqui)
        float pitch = player.getLocation().getPitch();
        if (pitch >= 45.0f) {
            return BlockFace.DOWN;
        }
        if (pitch <= -45.0f) {
            return BlockFace.UP;
        }
        return player.getFacing();
    }
}