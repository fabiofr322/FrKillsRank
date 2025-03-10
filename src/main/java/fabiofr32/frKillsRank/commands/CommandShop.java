package fabiofr32.frKillsRank.commands;

import fabiofr32.frKillsRank.gui.ShopGUI;
import fabiofr32.frKillsRank.managers.ShopManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CommandShop implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;

        // Se o jogador apenas digitar "/frloja", abre a loja
        if (args.length == 0) {
            ShopGUI.openMainShop(player);
            return true;
        }

        // Se o jogador digitar "/frloja sell <preço> <categoria>", adiciona o item na loja
        if (args.length == 3 && args[0].equalsIgnoreCase("sell")) {
            return sellItem(player, args[1], args[2]);
        }

        // Comando inválido
        player.sendMessage("§cUso correto:");
        player.sendMessage("§7/frloja §f- Abre a loja.");
        player.sendMessage("§7/frloja sell <preço> <categoria> §f- Adiciona um item à loja (Apenas Admins).");
        return true;
    }

    private boolean sellItem(Player player, String priceArg, String category) {
        // Verifica permissão
        if (!player.hasPermission("frloja.sell")) {
            player.sendMessage("§cVocê não tem permissão para adicionar itens à loja!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            player.sendMessage("§cVocê precisa segurar um item para vender na loja!");
            return true;
        }

        int price;
        try {
            price = Integer.parseInt(priceArg);
            if (price <= 0) {
                player.sendMessage("§cO preço deve ser maior que 0!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cPreço inválido! Use um número.");
            return true;
        }

        // Verifica se a categoria existe no shop.yml
        FileConfiguration shopConfig = ShopManager.getShopConfig();
        ConfigurationSection categorySection = shopConfig.getConfigurationSection("shop." + category);
        if (categorySection == null) {
            player.sendMessage("§cA categoria '" + category + "' não existe! Use uma categoria válida.");
            return true;
        }

        // Cria a chave única para armazenar o item na categoria correta
        String itemKey = "shop." + category + "." + UUID.randomUUID().toString().substring(0, 8);

        ItemMeta meta = item.getItemMeta();
        shopConfig.set(itemKey + ".name", meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name());
        shopConfig.set(itemKey + ".material", item.getType().toString());
        shopConfig.set(itemKey + ".price", price);

        if (meta.hasLore()) {
            shopConfig.set(itemKey + ".lore", meta.getLore());
        }

        if (meta.hasEnchants()) {
            List<String> enchants = new ArrayList<>();
            meta.getEnchants().forEach((enchant, level) ->
                    enchants.add(enchant.getKey().getKey().toUpperCase() + ":" + level)
            );
            shopConfig.set(itemKey + ".enchantments", enchants);
        }

        if (meta.hasCustomModelData()) {
            shopConfig.set(itemKey + ".custom_model_data", meta.getCustomModelData());
        }

        shopConfig.set(itemKey + ".unbreakable", meta.isUnbreakable());

        // Adiciona atributos personalizados, se houver
        Map<String, Map<String, Double>> attributes = new HashMap<>();
        for (Attribute attr : Attribute.values()) {
            AttributeInstance attrInstance = player.getAttribute(attr);
            if (attrInstance != null && !attrInstance.getModifiers().isEmpty()) {
                Map<String, Double> attrValues = new HashMap<>();
                for (AttributeModifier modifier : attrInstance.getModifiers()) {
                    attrValues.put(attr.name(), modifier.getAmount());
                }
                attributes.put("HAND", attrValues); // Assume que os atributos são para a mão do jogador
            }
        }
        shopConfig.set(itemKey + ".attributes", attributes);

        // Salva as alterações no arquivo (não recarrega, para não descartar as mudanças)
        ShopManager.saveShopConfig();
        ShopManager.reloadShopConfig();

        player.sendMessage("§aItem adicionado à loja na categoria '" + category + "' por " + price + " pontos!");
        return true;
    }
}
