package fabiofr32.frKillsRank.managers;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicMobsIntegrationManager {

    private final JavaPlugin plugin;
    private boolean promptShown = false;

    public MythicMobsIntegrationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkAndNotify() {
        Plugin mythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs");

        if (mythicMobs != null) {
            boolean showPrompt = ConfigManager.getBoolean("showMythicMobsPrompt", true);

            if (showPrompt && !promptShown) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("frkillsrank.admin")) {
                            sendMythicMobsPrompt(player);
                        }
                    }
                }, 40L); // Espera 2 segundos para evitar flood no login
            }
        }
    }

    private void sendMythicMobsPrompt(Player player) {
        String message = ConfigManager.getSimpleMessage("settings.mythicmobs.prompt_message");
        String clickableText = ConfigManager.getSimpleMessage("settings.mythicmobs.prompt_clickable");

        TextComponent messageComponent = new TextComponent(ChatColor.GREEN + message);
        TextComponent clickableComponent = new TextComponent(ChatColor.GOLD + clickableText);

        clickableComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/implementmm"));
        clickableComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ConfigManager.getSimpleMessage("settings.mythicmobs.prompt_hover")).create()));

        messageComponent.addExtra(clickableComponent);
        player.spigot().sendMessage(messageComponent);
        promptShown = true;
    }
}
