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

public class ConsumablesGUI implements Listener {

    private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&',
            ConfigManager.getSimpleMessage("settings.shop_gui.consumables.title")); // ✅ Agora pega do config.yml

    public static void openConsumablesShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Obtendo os itens da categoria "consumables" do ShopManager
        List<ItemStack> consumables = ShopManager.getShopItems("consumables");

        // Adiciona os itens ao GUI
        for (int i = 0; i < consumables.size() && i < 27; i++) {
            gui.setItem(i, consumables.get(i));
        }

        // Adiciona botão de fechar loja
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ConfigManager.getSimpleMessage("settings.shop_gui.close_button")); // ✅ Agora pegando do shop.yml
        closeItem.setItemMeta(closeMeta);
        gui.setItem(26, closeItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        // Verifica se o jogador está no GUI da Loja de Consumíveis
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY); // Bloqueia interações forçadas do jogador

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            // Se clicar no botão de fechar
            if (clickedItem.getType() == Material.BARRIER) {
                player.closeInventory();
                ShopGUI.openMainShop(player);
                return;
            }

            // Se clicar em um item da loja, tenta comprar
            if (ShopManager.purchaseItem(player, clickedItem)) {
            } else {
            }

        }
    }
}
