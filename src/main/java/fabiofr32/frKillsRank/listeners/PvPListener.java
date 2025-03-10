package fabiofr32.frKillsRank.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import fabiofr32.frKillsRank.managers.PlayerDataManager;
import fabiofr32.frKillsRank.managers.ConfigManager;

public class PvPListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // Verifica se o atacante e o atacado são jogadores
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacked = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        // Se o PvP do atacado estiver desativado, cancela o dano
        if (!PlayerDataManager.isPvPEnabled(attacked)) {
            event.setCancelled(true);
            attacker.sendMessage(ConfigManager.getSimpleMessage("settings.pvp.pvp_disabled_target"));
            return;
        }

        // Se o atacante estiver com PvP desativado, ele também não pode atacar
        if (!PlayerDataManager.isPvPEnabled(attacker)) {
            event.setCancelled(true);
            attacker.sendMessage(ConfigManager.getSimpleMessage("settings.pvp.pvp_disabled_attacker"));
        }
    }
}
