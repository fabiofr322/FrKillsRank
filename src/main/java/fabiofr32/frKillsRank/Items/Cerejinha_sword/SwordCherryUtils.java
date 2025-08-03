package fabiofr32.frKillsRank.Items.Cerejinha_sword;

import org.bukkit.inventory.ItemStack;

public class SwordCherryUtils {

    public static boolean isEspecialSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        var meta = item.getItemMeta();
        return meta != null && SwordCherry.NOME.equals(meta.getDisplayName());
    }
}
