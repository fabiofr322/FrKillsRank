package fabiofr32.frKillsRank.managers;

import fabiofr32.frKillsRank.FrKillsRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopManager {
    private static FileConfiguration shopConfig;

    public static void reloadShopConfig() {
        File shopFile = new File(FrKillsRank.getInstance().getDataFolder(), "shop.yml");

        if (!shopFile.exists()) {
            FrKillsRank.getInstance().saveResource("shop.yml", false);
        }

        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }

    public static FileConfiguration getShopConfig() {
        if (shopConfig == null) {
            reloadShopConfig();
        }
        return shopConfig;
    }




    public static void loadShopConfig() {
        FrKillsRank plugin = FrKillsRank.getInstance();
        File shopFile = new File(plugin.getDataFolder(), "shop.yml");

        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }

        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }

    public static List<ItemStack> getShopItems() {
        List<ItemStack> items = new ArrayList<>();
        ConfigurationSection shopSection = shopConfig.getConfigurationSection("shop.items");

        if (shopSection != null) {
            for (String key : shopSection.getKeys(false)) {
                String name = ChatColor.translateAlternateColorCodes('&', shopSection.getString(key + ".name", "Item"));
                Material material = Material.matchMaterial(shopSection.getString(key + ".material", "STONE"));
                int price = shopSection.getInt(key + ".price", 0);
                List<String> lore = shopSection.getStringList(key + ".lore");

                if (material == null) continue;

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);

                    // Aplicando lore personalizada
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }

                    // Pegando o formato do preço do config.yml
                    String priceFormat = ConfigManager.getSimpleMessage("settings.shop_gui.price_format");
                    priceFormat = priceFormat.replace("{price}", String.valueOf(price));
                    coloredLore.add(ChatColor.GOLD + priceFormat);

                    meta.setLore(coloredLore);

                    // Aplicando encantamentos personalizados
                    if (shopSection.contains(key + ".enchantments")) {
                        List<String> enchants = shopSection.getStringList(key + ".enchantments");
                        for (String ench : enchants) {
                            String[] parts = ench.split(":");
                            if (parts.length == 2) {
                                Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                                int level = Integer.parseInt(parts[1]);
                                if (enchantment != null) {
                                    meta.addEnchant(enchantment, level, true);
                                }
                            }
                        }
                    }

// Aplicando atributos personalizados
                    if (shopSection.contains(key + ".attributes")) {
                        ConfigurationSection attrSection = shopSection.getConfigurationSection(key + ".attributes");
                        if (attrSection != null) {
                            for (String slot : attrSection.getKeys(false)) {
                                EquipmentSlot equipmentSlot = EquipmentSlot.valueOf(slot.toUpperCase());
                                ConfigurationSection slotAttributes = attrSection.getConfigurationSection(slot);
                                if (slotAttributes != null) {
                                    for (String attrName : slotAttributes.getKeys(false)) {
                                        try {
                                            Attribute attribute = Attribute.valueOf(attrName.toUpperCase());
                                            double value = slotAttributes.getDouble(attrName);
                                            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), attrName, value, AttributeModifier.Operation.ADD_NUMBER, equipmentSlot);
                                            meta.addAttributeModifier(attribute, modifier);
                                        } catch (IllegalArgumentException e) {
                                            Bukkit.getLogger().warning("Atributo inválido: " + attrName);
                                        }
                                    }
                                }
                            }
                        }
                    }


                    // Definir Custom Model Data
                    if (shopSection.contains(key + ".custom_model_data")) {
                        meta.setCustomModelData(shopSection.getInt(key + ".custom_model_data"));
                    }

                    // Definir se o item é indestrutível
                    if (shopSection.contains(key + ".unbreakable")) {
                        meta.setUnbreakable(shopSection.getBoolean(key + ".unbreakable"));
                    }

                    item.setItemMeta(meta);
                }
                items.add(item);
            }
        }
        return items;
    }


    public static boolean purchaseItem(Player player, ItemStack item) {
        int price = extractPrice(item);
        if (price <= 0) return false;

        int playerPoints = ConfigManager.getPoints(player);
        if (playerPoints < price) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    ConfigManager.getSimpleMessage("settings.shop_gui.not_enough_points").replace("{price}", String.valueOf(price))));
            return false;
        }

        // Remove os pontos do jogador
        ConfigManager.removePoints(player, price);

        // Cria uma cópia do item sem a lore do preço
        ItemStack itemCopy = item.clone();
        ItemMeta meta = itemCopy.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                lore = lore.stream()
                        .filter(line -> !line.toLowerCase().contains("preço:") && !line.toLowerCase().contains("price:"))
                        .collect(Collectors.toList()); // Remove apenas a linha do preço
                meta.setLore(lore);
            }
            itemCopy.setItemMeta(meta);
        }

        // Adiciona o item sem preço ao inventário do jogador
        player.getInventory().addItem(itemCopy);

        // Mensagem de sucesso
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                ConfigManager.getSimpleMessage("settings.shop_gui.purchase_success")
                        .replace("{item}", item.getItemMeta().getDisplayName())
                        .replace("{price}", String.valueOf(price))
        ));
        return true;
    }


    private static int extractPrice(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String line : item.getItemMeta().getLore()) {
                if (line.toLowerCase().contains("preço:") || line.toLowerCase().contains("price:")) {
                    String[] split = line.split(":");
                    if (split.length > 1) {
                        try {
                            return Integer.parseInt(split[1].trim().replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException e) {
                            return -1;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public static void saveShopConfig() {
        try {
            File shopFile = new File(FrKillsRank.getInstance().getDataFolder(), "shop.yml");
            shopConfig.save(shopFile);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Não foi possível salvar a shop.yml!");
            e.printStackTrace();
        }
    }

}
