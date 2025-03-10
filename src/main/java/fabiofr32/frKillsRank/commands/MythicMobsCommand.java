package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class MythicMobsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public MythicMobsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("implementmm")) {
            if (sender.hasPermission("frkillsrank.admin")) {
                implementMythicMobsFiles();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm reload");
                sender.sendMessage(ChatColor.GREEN + ConfigManager.getSimpleMessage("settings.mythicmobs.implemented"));
                ConfigManager.setBoolean("showMythicMobsPrompt", false);
            }
            return true;
        }
        return false;
    }

    private void implementMythicMobsFiles() {
        try {
            // Copiar arquivos de MythicMobs para a pasta correta com prefixo "frkillsrank_"
            copyResource("frkillsrank_special_mobs.yml", "plugins/MythicMobs/mobs/frkillsrank_special_mobs.yml");
            copyResource("frkillsrank_spawners.yml", "plugins/MythicMobs/randomspawns/frkillsrank_spawners.yml");
            copyResource("frkillsrank_skills.yml", "plugins/MythicMobs/skills/frkillsrank_skills.yml");
            copyResource("frkillsrank_drops.yml", "plugins/MythicMobs/droptables/frkillsrank_drops.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyResource(String resourceName, String destination) throws IOException {
        InputStream in = plugin.getResource(resourceName);
        if (in != null) {
            File outFile = new File(destination);
            if (!outFile.exists()) {
                Files.copy(in, outFile.toPath());
            }
        }
    }
}