package fabiofr32.frKillsRank.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import fabiofr32.frKillsRank.managers.PlayerDataManager;

public class PvPListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // Verifica se a entidade atingida é um jogador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacked = (Player) event.getEntity();
        Player attacker = null;

        // Se o dano foi causado diretamente por um jogador...
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }
        // ...ou por um projétil disparado por um jogador
        else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        // Se não identificamos um atacante do tipo Player, retorna
        if (attacker == null) {
            return;
        }

        // Se o PvP do jogador atingido estiver desativado, cancela o dano
        if (!PlayerDataManager.isPvPEnabled(attacked)) {
            event.setCancelled(true);
            attacker.sendMessage("§cEste jogador tem o PvP desativado!");
            return;
        }

        // Se o atacante estiver com PvP desativado, ele não pode atacar
        if (!PlayerDataManager.isPvPEnabled(attacker)) {
            event.setCancelled(true);
            attacker.sendMessage("§cVocê tem o PvP desativado e não pode atacar outros jogadores!");
        }
    }
}
