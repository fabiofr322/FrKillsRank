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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.meta.PotionMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopManager {
    private static FileConfiguration shopConfig;

    public static void loadShopConfig() {
        FrKillsRank plugin = FrKillsRank.getInstance();
        File shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }

    public static void reloadShopConfig() {
        File shopFile = new File(FrKillsRank.getInstance().getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            FrKillsRank.getInstance().saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }

    public static void saveShopConfig() {
        try {
            File shopFile = new File(FrKillsRank.getInstance().getDataFolder(), "shop.yml");
            shopConfig.save(shopFile);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Erro ao salvar shop.yml!");
            e.printStackTrace();
        }
    }

    public static FileConfiguration getShopConfig() {
        if (shopConfig == null) {
            reloadShopConfig();
        }
        return shopConfig;
    }

    /**
     * Busca os itens da loja de uma categoria específica.
     * Exemplo: getShopItems("consumables") para pegar os consumíveis.
     */
    public static List<ItemStack> getShopItems(String category) {
        List<ItemStack> items = new ArrayList<>();
        ConfigurationSection shopSection = shopConfig.getConfigurationSection("shop." + category);
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

                    // Adiciona a lore personalizada
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }

                    // Adiciona o preço ao final da lore
                    String priceFormat = ConfigManager.getSimpleMessage("settings.shop_gui.price_format");
                    priceFormat = priceFormat.replace("{price}", String.valueOf(price));
                    coloredLore.add(ChatColor.GOLD + priceFormat);

                    meta.setLore(coloredLore);

                    // Aplica encantamentos personalizados, se houver
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

                    // Aplica atributos personalizados, se houver
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

                    // Define Custom Model Data, se configurado
                    if (shopSection.contains(key + ".custom_model_data")) {
                        meta.setCustomModelData(shopSection.getInt(key + ".custom_model_data"));
                    }

                    // Define se o item é indestrutível
                    if (shopSection.contains(key + ".unbreakable")) {
                        meta.setUnbreakable(shopSection.getBoolean(key + ".unbreakable"));
                    }

                    item.setItemMeta(meta);
                }

                // Se o item for uma poção, aplica os efeitos personalizados
                if (material == Material.POTION || material == Material.SPLASH_POTION) {
                    applyPotionEffects(item, shopSection.getConfigurationSection(key));
                }

                items.add(item);
            }
        }
        return items;
    }

    /**
     * Aplica os efeitos de poção personalizados a um item do tipo POTION ou SPLASH_POTION.
     * O efeito deve estar definido na seção "effects" do item no shop.yml, no formato "EFFECT:duration:amplifier".
     * Exemplo: "SPEED:180:1" para um efeito SPEED de 180 segundos no nível 1.
     */
    private static void applyPotionEffects(ItemStack item, ConfigurationSection section) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return;
        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        if (section != null && section.contains("effects")) {
            List<String> effectsList = section.getStringList("effects");
            for (String effectString : effectsList) {
                String[] parts = effectString.split(":");
                if (parts.length == 3) {
                    PotionEffectType effectType = PotionEffectType.getByName(parts[0].toUpperCase());
                    int durationSeconds = Integer.parseInt(parts[1]);
                    int amplifier = Integer.parseInt(parts[2]);
                    int durationTicks = durationSeconds * 20; // Converte segundos para ticks
                    if (effectType != null) {
                        potionMeta.addCustomEffect(new PotionEffect(effectType, durationTicks, amplifier), true);
                    }
                }
            }
        }
        item.setItemMeta(potionMeta);
    }

    public static boolean purchaseItem(Player player, ItemStack item) {
        int price = extractPrice(item);
        if (price <= 0) return false;
        int playerPoints = ConfigManager.getPoints(player);
        if (playerPoints < price) {
            player.sendMessage(ConfigManager.getMessage("setting.shop_gui.not_enough_points", player, price));
            return false;
        }

        // Verifica se há espaço no inventário do jogador
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ConfigManager.getSimpleMessage("settings.shop_gui.inventory_full"));
            return false;
        }

        // Remove os pontos do jogador
        ConfigManager.removePoints(player, price);

        // Cria uma cópia do item para remover a linha do preço na lore
        ItemStack itemCopy = item.clone();
        ItemMeta meta = itemCopy.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                lore = lore.stream()
                        .filter(line -> !line.toLowerCase().contains("preço:") && !line.toLowerCase().contains("price:"))
                        .collect(Collectors.toList());
                meta.setLore(lore);
            }
            itemCopy.setItemMeta(meta);
        }

        // Adiciona o item ao inventário do jogador
        player.getInventory().addItem(itemCopy);

        // Mensagem de sucesso
        String itemName = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : item.getType().name();
        String successMessage = ConfigManager.getMessage("shop_gui.purchase_success", player, price)
                .replace("{item}", itemName)
                .replace("{price}", String.valueOf(price));

        player.sendMessage(successMessage);
        return true;
    }

    public static int extractPrice(ItemStack item) {
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

    public static int getItemPriceFromConfig(String category, String itemName) {
        ConfigurationSection shopSection = shopConfig.getConfigurationSection("shop." + category);
        if (shopSection != null) {
            for (String key : shopSection.getKeys(false)) {
                String configItemName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', shopSection.getString(key + ".name", "")));
                if (configItemName.equalsIgnoreCase(ChatColor.stripColor(itemName))) {
                    return shopSection.getInt(key + ".price", 0);
                }
            }
        }
        return -1;
    }

    public static String getShopMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', shopConfig.getString(key, "&cMensagem não encontrada: " + key));
    }
}
