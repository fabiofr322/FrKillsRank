package fabiofr32.frKillsRank.Items.Trident;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class TridenteUtils {

    public static ItemStack criarTridente(FrKillsRank plugin) {
        ItemStack tridente = new ItemStack(Material.TRIDENT);
        ItemMeta meta = tridente.getItemMeta();

        meta.setDisplayName("§9Tridente da Maré Eterna");
        meta.setLore(List.of(
                "§7Um tridente encantado pelos deuses do oceano.",
                "",
                "§bHabilidades Ativas:",
                "§8» §fRespiração aquática",
                "§8» §fGraça do Golfinho",
                "§8» §fVelocidade ao nadar",
                "§8» §fDash na água (Shift + botão esquerdo)",
                "§8» §fAumenta o dano ao ser arremessado na água"
        ));
        meta.addEnchant(Enchantment.LOYALTY, 5, true);
        meta.addEnchant(Enchantment.IMPALING, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        //meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.isUnbreakable();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "tridente_especial"), PersistentDataType.BYTE, (byte) 1);
        tridente.setItemMeta(meta);

        return tridente;
    }

    public static boolean isTridenteEspecial(ItemStack item, FrKillsRank plugin) {
        if (item == null || item.getType() != Material.TRIDENT) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "tridente_especial"), PersistentDataType.BYTE);
    }
}
