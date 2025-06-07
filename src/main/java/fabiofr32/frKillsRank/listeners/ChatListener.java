package fabiofr32.frKillsRank.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import fabiofr32.frKillsRank.managers.TagManager;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String mensagem = event.getMessage();
        event.setFormat(TagManager.formatChat(player, mensagem));
    }
}
