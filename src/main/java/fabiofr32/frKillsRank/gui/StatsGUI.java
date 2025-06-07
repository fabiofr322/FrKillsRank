package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class StatsGUI implements Listener {

    private static final String GUI_TITLE = ConfigManager.getSimpleMessage("settings.frstats.gui.guiTitle");

    public static void openStatsGUI(Player player) {
        String finalTitle = GUI_TITLE.replace("{player}", player.getName());
        Inventory gui = Bukkit.createInventory(new StatsGUIHolder(null), 54, finalTitle); // GUI 6 linhas (9x6)

        // Dados do jogador
        int totalPoints = PlayerDataManager.getPoints(player);
        int mobsKilled = PlayerDataManager.getKills(player);
        int bestStreak = PlayerDataManager.getStreak(player);
        int pvpKills = PlayerDataManager.getPvPKills(player);
        int pvpDeaths = PlayerDataManager.getPvPDeaths(player);
        String kdRatio = PlayerDataManager.getKDRatio(player);
        double highestDamage = PlayerDataManager.getHighestDamage(player);
        double highestDamageTaken = PlayerDataManager.getHighestDamageTaken(player);
        long playTime = PlayerDataManager.getSavedPlayTime(player);
        String formattedPlayTime = PlayerDataManager.formatPlayTime(playTime);
        String lastMob = PlayerDataManager.getLastMobKilled(player);
        String mostKilledMob = PlayerDataManager.getMostKilledMob(player);
        int mostKilledMobCount = PlayerDataManager.getMostKilledMobCount(player);
        int deaths = PlayerDataManager.getDeaths(player);
        String lastDeath = PlayerDataManager.getLastDeath(player);

        // Distribuição dos itens
        gui.setItem(10, createStatItem(Material.EMERALD, "totalPoints", totalPoints));
        gui.setItem(12, createStatItem(Material.DIAMOND_SWORD, "mobsKilled", mobsKilled));
        gui.setItem(14, createStatItem(Material.END_CRYSTAL, "bestStreak", bestStreak));
        gui.setItem(16, createStatItem(Material.IRON_SWORD, "pvpKills", pvpKills));

        gui.setItem(28, createStatItem(Material.TOTEM_OF_UNDYING, "pvpDeaths", pvpDeaths));
        gui.setItem(30, createStatItem(Material.PAPER, "kdRatio", kdRatio));
        gui.setItem(32, createStatItem(Material.ANVIL, "highestDamage", highestDamage));
        gui.setItem(34, createStatItem(Material.SHIELD, "highestDamageTaken", highestDamageTaken));

        gui.setItem(20, createStatItem(Material.CLOCK, "playTime", formattedPlayTime));
        gui.setItem(22, createStatItem(Material.ZOMBIE_HEAD, "lastMob", lastMob));
        gui.setItem(24, createStatItem(Material.SPIDER_EYE, "mostKilledMob", mostKilledMob + " (&e" + mostKilledMobCount + " mortes&f)"));
        gui.setItem(38, createStatItem(Material.SKULL_BANNER_PATTERN, "deaths", deaths));
        gui.setItem(40, createStatItem(Material.PLAYER_HEAD, "lastDeath", lastDeath));

        // Botão de fechar
        gui.setItem(49, createStatItem(Material.BARRIER, "close", "Clique para fechar"));

        fillEmptySlots(gui);
        player.openInventory(gui);
    }

    private static ItemStack createStatItem(Material material, String configKey, Object value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            String displayName = ConfigManager.getSimpleMessage("settings.frstats.gui." + configKey + ".name");
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);

            String loreText = ConfigManager.getSimpleMessage("settings.frstats.gui." + configKey + ".lore");
            if (loreText != null) {
                loreText = ChatColor.translateAlternateColorCodes('&', loreText.replace("{value}", String.valueOf(value)));
            } else {
                loreText = ChatColor.RED + "Mensagem não encontrada!";
            }

            meta.setDisplayName(displayName);
            meta.setLore(Arrays.asList(loreText));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void fillEmptySlots(Inventory inventory) {
        ItemStack decoration = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = decoration.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            decoration.setItemMeta(meta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, decoration);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof StatsGUIHolder) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
                event.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof StatsGUIHolder) {
            // Ação ao fechar o inventário, se desejar
        }
    }
}
