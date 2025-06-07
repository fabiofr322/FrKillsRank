package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

public class MainGUI {

    public static void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.main_title", "&b&lMenu Principal")));
        
        // Item 1: Cabeça do jogador (slot 10)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        // Define a cabeça como sendo do jogador
        headMeta.setOwningPlayer(player);

        // Nome do item (do config)
        String headName = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.player_head_name", "&aSeu Perfil"));
        headMeta.setDisplayName(headName);

        // Pega os pontos e o rank corretamente
        int points = ConfigManager.getPoints(player);
        String rank = ConfigManager.getRank(player); // <- usa o método correto agora

        // Lore com cores e variáveis
        List<String> headLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.player_head_lore");
        headLore = headLore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)
                        .replace("{rank}", rank)
                        .replace("{points}", String.valueOf(points)))
                .collect(Collectors.toList());

        headMeta.setLore(headLore);
        head.setItemMeta(headMeta);
        gui.setItem(20, head);

        // Item 1: Loja
        ItemStack loja = new ItemStack(Material.EMERALD);
        ItemMeta lojaMeta = loja.getItemMeta();
        lojaMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.shop_item_name", "&aLoja de Pontos")));
        List<String> lojaLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.shop_item_lore");
        lojaLore = lojaLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        lojaMeta.setLore(lojaLore);
        lojaMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        loja.setItemMeta(lojaMeta);
        gui.setItem(21, loja);

        // Item 2: Recompensas
        ItemStack recompensas = new ItemStack(Material.NETHER_STAR);
        ItemMeta recompensaMeta = recompensas.getItemMeta();
        recompensaMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.rank_item_name", "&dRank dos Jogadores")));
        List<String> recompensaLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.rewards_item_lore");
        recompensaLore = recompensaLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        recompensaMeta.setLore(recompensaLore);
        recompensaMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        recompensas.setItemMeta(recompensaMeta);
        gui.setItem(22, recompensas);

        // Item 3: Estatísticas (novo!)
        ItemStack stats = new ItemStack(Material.COMPASS);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.stats_item_name", "&9Estatísticas")));
        List<String> statsLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.stats_item_lore");
        statsLore = statsLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        statsMeta.setLore(statsLore);
        statsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stats.setItemMeta(statsMeta);
        gui.setItem(23, stats);

        // Item 4: Missões
        ItemStack missoes = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta missaoMeta = missoes.getItemMeta();
        missaoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.quests_item_name", "&dMissões")));
        List<String> missaoLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.quests_item_lore");
        missaoLore = missaoLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        missaoMeta.setLore(missaoLore);
        missaoMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        missoes.setItemMeta(missaoMeta);
        gui.setItem(24, missoes);
        
        // Item 4
        ItemStack fechar = new ItemStack(Material.BARRIER);
        ItemMeta fecharMeta = fechar.getItemMeta();
        fecharMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.frstatsgui.close.name", "&c❌ Fechar")));
        List<String> fecharLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.frstatsgui.close.lore");
        fecharLore = fecharLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        fecharMeta.setLore(fecharLore);
        fecharMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        fechar.setItemMeta(fecharMeta);
        gui.setItem(40, fechar);



        // Decoração
        fillEmptySlots(gui);

        player.openInventory(gui);
    }

    private static void fillEmptySlots(Inventory gui) {
        ItemStack vidro = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = vidro.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            vidro.setItemMeta(meta);
        }

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, vidro);
            }
        }
    }
}
