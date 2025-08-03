package fabiofr32.frKillsRank.Items.SonicDashSword;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class GodwardenSwordItem {

    public static ItemStack create(JavaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();

        meta.setDisplayName("§5Godwarden Sword");

        meta.setLore(Arrays.asList(
                "§7Sharpness V, Fire Aspect II, Knockback II, Unbreaking III",
                "",
                "§8Passive Ability: §cSonic Shadow Strike",
                "§7Shift + Left Click to dash and release a",
                "§7sonic wave that damages and slows enemies."
        ));

        meta.addEnchant(Enchantment.SHARPNESS, 6, true);
        //meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        //meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);

        NamespacedKey key = new NamespacedKey(plugin, "godwarden_sword");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.BYTE, (byte) 1);

        sword.setItemMeta(meta);
        return sword;
    }
}
