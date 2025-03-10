package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {

    public static void openMainShop(Player player) {
        // Pega o título da loja do config.yml
        String shopTitle = ChatColor.translateAlternateColorCodes('&', ConfigManager.getSimpleMessage("settings.shop_gui.title"));
        Inventory gui = Bukkit.createInventory(null, 27, shopTitle);

        // Cabeça do jogador com estatísticas
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
        headMeta.setOwningPlayer(player);
        headMeta.setDisplayName(ChatColor.GREEN + player.getName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Pontos: " + ChatColor.WHITE + ConfigManager.getPoints(player));
        lore.add(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + ConfigManager.getRank(player));
        headMeta.setLore(lore);
        playerHead.setItemMeta(headMeta);

        gui.setItem(4, playerHead);

        // Obtendo nomes e lores das categorias definidas no config.yml
        gui.setItem(11, createCategoryItem(Material.GOLDEN_APPLE, "consumables"));
        gui.setItem(12, createCategoryItem(Material.DIAMOND_CHESTPLATE, "armors"));
        gui.setItem(13, createCategoryItem(Material.DIAMOND_SWORD, "swords"));
        gui.setItem(14, createCategoryItem(Material.TOTEM_OF_UNDYING, "support_items"));

        int points = ConfigManager.getPoints(player);

        // Pegando informações das lojas exclusivas definidas no config.yml
        gui.setItem(15, createRankedShopItem(Material.ENCHANTED_BOOK, "master", points));
        gui.setItem(16, createRankedShopItem(Material.NETHER_STAR, "legendary", points));

        player.openInventory(gui);
    }

    private static ItemStack createCategoryItem(Material material, String category) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Puxa nome e lore do config.yml utilizando as chaves corretas
        String name = ConfigManager.getSimpleMessage("settings.shop_gui.categories." + category + ".name");
        List<String> lore = ConfigManager.getMessageList("settings.shop_gui.categories." + category + ".lore");

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> translatedLore = new ArrayList<>();
        for (String line : lore) {
            translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(translatedLore);

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createRankedShopItem(Material material, String rank, int playerPoints) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Puxa nome e lore do config.yml utilizando as chaves corretas
        String name = ConfigManager.getSimpleMessage("settings.shop_gui.ranked_shops." + rank + ".name");
        List<String> lore = ConfigManager.getMessageList("settings.shop_gui.ranked_shops." + rank + ".lore");

        int requiredPoints = ConfigManager.getRankPoints(rank);
        boolean hasAccess = playerPoints >= requiredPoints;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> translatedLore = new ArrayList<>();
        for (String line : lore) {
            translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        // Adiciona aviso caso o jogador não tenha acesso à loja
        if (!hasAccess) {
            translatedLore.add("");
            translatedLore.add(ChatColor.RED + "Requer " + requiredPoints + " pontos para acessar!");
        }

        meta.setLore(translatedLore);
        item.setItemMeta(meta);
        return item;
    }
}
