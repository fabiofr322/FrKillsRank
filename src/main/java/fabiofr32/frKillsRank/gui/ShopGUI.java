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

public class ShopGUI {

    public static void openMainShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&',
        		ConfigManager.getSimpleMessage("settings.shop_gui.title")));

        // Cabeça do jogador (slot 20)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(player);
        String headName = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.gui.player_head_name", "&aSeu Perfil"));
        headMeta.setDisplayName(headName);
        int points = ConfigManager.getPoints(player);
        String rank = ConfigManager.getRank(player);
        List<String> headLore = FrKillsRank.getInstance().getConfig().getStringList("settings.gui.player_head_lore");
        headLore = headLore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)
                        .replace("{rank}", rank)
                        .replace("{points}", String.valueOf(points)))
                .collect(Collectors.toList());
        headMeta.setLore(headLore);
        headMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        head.setItemMeta(headMeta);
        gui.setItem(20, head);

        // Loja de consumíveis
        gui.setItem(21, createCategoryItem(Material.GOLDEN_APPLE, "consumables"));

        // Loja de armaduras
        gui.setItem(22, createCategoryItem(Material.DIAMOND_CHESTPLATE, "armors"));

        // Loja de espadas
        gui.setItem(23, createCategoryItem(Material.DIAMOND_SWORD, "swords"));

        // Loja de suporte
        gui.setItem(24, createCategoryItem(Material.TOTEM_OF_UNDYING, "support_items"));

        // Loja Mestre
        gui.setItem(30, createRankedShopItem(Material.ENCHANTED_BOOK, "master", points));

        // Loja Lendária
        gui.setItem(31, createRankedShopItem(Material.NETHER_STAR, "legendary", points));

        // Botão de fechar (slot 40)
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

        // Preenche os espaços vazios com vidro
        fillEmptySlots(gui);

        player.openInventory(gui);
    }

    private static ItemStack createCategoryItem(Material material, String category) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String name = ConfigManager.getSimpleMessage("settings.shop_gui.categories." + category + ".name");
        List<String> lore = ConfigManager.getMessageList("settings.shop_gui.categories." + category + ".lore");

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createRankedShopItem(Material material, String rank, int playerPoints) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String name = ConfigManager.getSimpleMessage("settings.shop_gui.ranked_shops." + rank + ".name");
        List<String> lore = ConfigManager.getMessageList("settings.shop_gui.ranked_shops." + rank + ".lore");

        int requiredPoints = ConfigManager.getRankPoints(rank);
        boolean hasAccess = playerPoints >= requiredPoints;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());

        if (!hasAccess) {
            lore.add("");
            lore.add(ChatColor.RED + "Requer " + requiredPoints + " pontos para acessar!");
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
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
