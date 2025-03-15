package fabiofr32.frKillsRank.commands;

import com.google.common.collect.Multimap;
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
import org.bukkit.inventory.EquipmentSlot;
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
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("sell")) {
                return sellItem(player, args[1], args[2]);
            } else {
                player.sendMessage("§cComando inválido! Você quis dizer: §7/frloja sell <preço> <categoria>?");
                return true;
            }
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
        ConfigurationSection shopSection = shopConfig.getConfigurationSection("shop");

        if (shopSection == null || !shopSection.contains(category)) {
            player.sendMessage("§cA categoria '" + category + "' não existe!");

            // Lista as categorias disponíveis
            if (shopSection != null) {
                player.sendMessage("§eCategorias disponíveis:");
                for (String cat : shopSection.getKeys(false)) {
                    player.sendMessage(" §6- " + cat);
                }
            } else {
                player.sendMessage("§cNenhuma categoria disponível no momento.");
            }
            return true;
        }

        // Cria a chave única para armazenar o item na categoria correta
        String itemKey = category + "." + UUID.randomUUID().toString().substring(0, 8);

        ItemMeta meta = item.getItemMeta();
        shopConfig.set("shop." + itemKey + ".name", (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : item.getType().name());
        shopConfig.set("shop." + itemKey + ".material", item.getType().toString());
        shopConfig.set("shop." + itemKey + ".price", price);

        if (meta != null && meta.hasLore()) {
            shopConfig.set("shop." + itemKey + ".lore", meta.getLore());
        }

        if (meta != null && meta.hasEnchants()) {
            List<String> enchants = new ArrayList<>();
            meta.getEnchants().forEach((enchant, level) ->
                    enchants.add(enchant.getKey().getKey().toUpperCase() + ":" + level)
            );
            shopConfig.set("shop." + itemKey + ".enchantments", enchants);
        }

        if (meta != null && meta.hasCustomModelData()) {
            shopConfig.set("shop." + itemKey + ".custom_model_data", meta.getCustomModelData());
        }

        if (meta != null) {
            shopConfig.set("shop." + itemKey + ".unbreakable", meta.isUnbreakable());

            // Pegando atributos do item corretamente
            Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
            if (modifiers != null && !modifiers.isEmpty()) {
                Map<String, Map<String, Double>> attributes = new HashMap<>();
                for (Attribute attribute : modifiers.keySet()) {
                    for (AttributeModifier modifier : modifiers.get(attribute)) {
                        EquipmentSlot slot = modifier.getSlot();
                        if (slot == null) {
                            slot = EquipmentSlot.HAND;
                        }
                        String slotKey = slot.name();
                        Map<String, Double> attrValues = attributes.getOrDefault(slotKey, new HashMap<>());
                        attrValues.put(attribute.name(), modifier.getAmount());
                        attributes.put(slotKey, attrValues);
                    }
                }
                shopConfig.set("shop." + itemKey + ".attributes", attributes);
            }
        }

        // Salva as alterações no arquivo e recarrega o shopConfig
        ShopManager.saveShopConfig();
        ShopManager.reloadShopConfig();

        player.sendMessage("§aItem adicionado à loja na categoria '" + category + "' por " + price + " pontos!");
        return true;
    }

}
