package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.ConfigManager;
import fabiofr32.frKillsRank.managers.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopGUI implements Listener {

    public static void openShop(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.shop_gui.shop_gui_title", "&6&lLoja de Pontos"));
        Inventory shopInventory = Bukkit.createInventory(null, 27, title);

        // Item 1: Cabeça do jogador (slot 10)
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
        head.setItemMeta(headMeta);
        shopInventory.setItem(4, head);

        // Adiciona os itens da loja
        int slot = 9;
        for (ItemStack item : ShopManager.getShopItems()) {
            shopInventory.setItem(slot++, item);
            if (slot >= shopInventory.getSize()) break;
        }


        player.openInventory(shopInventory);
    }

    @EventHandler
    public void onShopItemClick(InventoryClickEvent event) {
        String title = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.shop_gui.shop_gui_title", "&6&lLoja de Pontos"));
        if (event.getView().getTitle().equals(title)) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType().isAir()) return;

            if (ShopManager.purchaseItem(player, clickedItem)) {
            }
        }
    }

}
