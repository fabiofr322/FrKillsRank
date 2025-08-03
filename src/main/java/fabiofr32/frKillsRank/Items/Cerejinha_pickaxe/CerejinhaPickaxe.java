package fabiofr32.frKillsRank.Items.Cerejinha_pickaxe;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class CerejinhaPickaxe {
    // Picareta da Cerejinha
    public static ItemStack getCerejinhaPickaxe() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        NamespacedKey key = new NamespacedKey("frkillsrank", "cerejinha_pickaxe");
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);


        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Picareta da " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Cerejinha");

            meta.setLore(Arrays.asList(
                    ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Um presente do seu amor Fabio!"
            ));

            meta.setCustomModelData(1001);

            meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
            meta.addEnchant(Enchantment.UNBREAKING, 8, true);
            meta.addEnchant(Enchantment.FORTUNE, 6, true);

            //meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            meta.setUnbreakable(true);

            item.setItemMeta(meta);
        }

        return item;
    }
}
