package fabiofr32.frKillsRank.Items.Cerejinha_sword;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SwordCherry {

    public static final String NOME_RAW = "&f&lEspada &5&lDa &d&lCerejinha";
    public static final String NOME = ChatColor.translateAlternateColorCodes('&', NOME_RAW);

    public static ItemStack criar() {
        ItemStack espada = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = espada.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(NOME);
            meta.setLore(List.of(
                    ChatColor.translateAlternateColorCodes('&', "&dFeita com carinho para a dona do meu coração."),
                    ChatColor.translateAlternateColorCodes('&', "&f&lShift + Clique Direito: &dSalto com cerejeiras"),
                    ChatColor.translateAlternateColorCodes('&', "&f&lClique Esquerdo no ar: &dDash encantado")
            ));
            meta.addEnchant(Enchantment.SHARPNESS, 32, true);
            meta.addEnchant(Enchantment.LOOTING, 8, true);
            meta.addEnchant(Enchantment.SWEEPING_EDGE, 4, true);
            meta.setUnbreakable(true);
            espada.setItemMeta(meta);
        }

        return espada;
    }
}
