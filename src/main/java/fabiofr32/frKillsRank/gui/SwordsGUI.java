package fabiofr32.frKillsRank.gui;

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

import java.util.List;

public class SwordsGUI implements Listener {

    private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&',
            ConfigManager.getSimpleMessage("settings.shop_gui.categories.swords.name"));

    public static void openSwordsShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);
        List<ItemStack> swords = ShopManager.getShopItems("swords");
        for (int i = 0; i < swords.size() && i < 27; i++) {
            gui.setItem(i, swords.get(i));
        }
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ConfigManager.getSimpleMessage("shop_gui.close_button"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(26, closeItem);
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            if (clickedItem.getType() == Material.BARRIER) {
                player.closeInventory();
                ShopGUI.openMainShop(player);
                return;
            }
            if (ShopManager.purchaseItem(player, clickedItem)) {} else {}
        }
    }
}
