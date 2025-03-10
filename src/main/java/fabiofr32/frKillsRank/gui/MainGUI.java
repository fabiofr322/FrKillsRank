package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

public class MainGUI {

    public static void openMainGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.main_title", "&6FrKillsRank Menu"));
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Item 1: Cabeça do jogador (slot 10)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(player);
        String headName = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.player_head_name", "&aSeu Perfil"));
        headMeta.setDisplayName(headName);
        int points = ConfigManager.getPoints(player);
        String rank = getRank(points);
        List<String> headLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.player_head_lore");
        headLore = headLore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)
                        .replace("{rank}", rank)
                        .replace("{points}", String.valueOf(points)))
                .collect(Collectors.toList());
        headMeta.setLore(headLore);
        head.setItemMeta(headMeta);
        gui.setItem(10, head);

        // Item 2: Recompensas (slot 12)
        ItemStack rewards = new ItemStack(Material.CHEST);
        ItemMeta rewardsMeta = rewards.getItemMeta();
        String rewardsName = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.shop_item_name", "&bLoja"));
        rewardsMeta.setDisplayName(rewardsName);
        List<String> rewardsLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.shop_item_lore");
        rewardsLore = rewardsLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        rewardsMeta.setLore(rewardsLore);
        rewards.setItemMeta(rewardsMeta);
        gui.setItem(12, rewards);

        // Item 3: Rank dos jogadores (slot 14)
        ItemStack rankItem = new ItemStack(Material.PAPER);
        ItemMeta rankMeta = rankItem.getItemMeta();
        String rankItemName = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.rank_item_name", "&dRank dos Jogadores"));
        rankMeta.setDisplayName(rankItemName);
        List<String> rankLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.rank_item_lore");
        rankLore = rankLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        rankMeta.setLore(rankLore);
        rankItem.setItemMeta(rankMeta);
        gui.setItem(14, rankItem);

        // Item 4: Missões (slot 16)
        ItemStack missions = new ItemStack(Material.BOOK);
        ItemMeta missionsMeta = missions.getItemMeta();
        String missionsName = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.missions_item_name", "&eMissões"));
        missionsMeta.setDisplayName(missionsName);
        List<String> missionsLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.missions_item_lore");
        missionsLore = missionsLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        missionsMeta.setLore(missionsLore);
        missions.setItemMeta(missionsMeta);
        gui.setItem(16, missions);

        player.openInventory(gui);
    }

    // Exemplo simples para calcular rank com base em pontos
    private static String getRank(int points) {
        if (points < 500) {
            return "Iniciante";
        } else if (points < 2000) {
            return "Guerreiro";
        } else {
            return "Lendário";
        }
    }
}
