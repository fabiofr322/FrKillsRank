package fabiofr32.frKillsRank.Items.Crossbow;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;

public class BestaUtils {

    public static ItemStack criarBestaFumaca(FrKillsRank plugin) {
        ItemStack besta = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = besta.getItemMeta();

        // Nome e Lore Atualizados
        meta.setDisplayName("§7Besta da Fumaça Asfixiante");
        meta.setLore(List.of(
                "§7Forjada com ervas venenosas da floresta sombria.",
                "",
                "§8Habilidades:",
                "§8» §fDeixa um rastro de fumaça tóxica",
                "§8» §fInimigos no rastro sofrem §eLentidão II",
                "§8» §fNo impacto, causa §cCegueira"
        ));

        // Encantamentos Ajustados
        meta.addEnchant(Enchantment.QUICK_CHARGE, 2, true); // Recarga mais lenta que a versão explosiva
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Tag Customizada (NBT) - Agora identifica a besta de fumaça
        NamespacedKey key = new NamespacedKey(plugin, "besta_fumaca");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        besta.setItemMeta(meta);
        return besta;
    }

    public static boolean isBestaFumaca(ItemStack item, FrKillsRank plugin) {
        if (item == null || item.getType() != Material.CROSSBOW) return false;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "besta_fumaca");
        return meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}