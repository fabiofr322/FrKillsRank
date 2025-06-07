package fabiofr32.frKillsRank.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class StatsGUIHolder implements InventoryHolder {
    private final Inventory inventory;

    public StatsGUIHolder(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
