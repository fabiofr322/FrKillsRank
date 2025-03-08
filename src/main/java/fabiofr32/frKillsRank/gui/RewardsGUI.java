package fabiofr32.frKillsRank.gui;

import fabiofr32.frKillsRank.FrKillsRank;
import fabiofr32.frKillsRank.managers.RewardsManager;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RewardsGUI implements Listener {

    public static void openRewardsMenu(Player player) {
        // Puxa o título da GUI do config.yml
        String guiTitle = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.rewards.gui_title", "&6Recompensas Disponíveis"));
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);

        List<Integer> availableRewards = RewardsManager.getAvailableRewards(player);

        for (int rewardPoints : availableRewards) {
            ItemStack item = new ItemStack(Material.DIAMOND);
            ItemMeta meta = item.getItemMeta();
            // Puxa o display name configurável, com placeholder {points}
            String displayName = ChatColor.translateAlternateColorCodes('&',
                            FrKillsRank.getInstance().getConfig().getString("settings.rewards.item_display", "&aRecompensa por {points} pontos!"))
                    .replace("{points}", String.valueOf(rewardPoints));
            meta.setDisplayName(displayName);
            // Puxa a lore configurável; se não existir, usa o valor padrão
            List<String> lore = FrKillsRank.getInstance().getConfig().getStringList("settings.rewards.item_lore");
            if (lore.isEmpty()) {
                lore = Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&eClique para resgatar!"));
            } else {
                lore = lore.stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            gui.addItem(item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        // Puxa o título configurável para verificar se é a GUI de recompensas
        String guiTitle = ChatColor.translateAlternateColorCodes('&',
                FrKillsRank.getInstance().getConfig().getString("settings.rewards.gui_title", "&6Recompensas Disponíveis"));
        if (!event.getView().getTitle().equals(guiTitle))
            return;

        event.setCancelled(true);

        // Verifica se o item clicado é nulo ou não possui meta ou display name
        if (event.getCurrentItem() == null ||
                !event.getCurrentItem().hasItemMeta() ||
                event.getCurrentItem().getItemMeta().getDisplayName() == null) {
            return;
        }

        String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
        String digits = ChatColor.stripColor(displayName).replaceAll("\\D+", "");

        // Se não houver dígitos, não faz nada
        if (digits.isEmpty()) {
            return;
        }

        int points = Integer.parseInt(digits);

        if (RewardsManager.claimReward(player, points)) {
            String claimedMessage = ChatColor.translateAlternateColorCodes('&',
                    FrKillsRank.getInstance().getConfig().getString("settings.rewards.claimed_message", "&aRecompensa resgatada!"));
            player.sendMessage(claimedMessage);
        }
    }
}
