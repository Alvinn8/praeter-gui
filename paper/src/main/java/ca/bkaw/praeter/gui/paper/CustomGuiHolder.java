package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.gui.CustomGui;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link InventoryHolder} for a custom gui.
 * <p>
 * Can be used to identify the inventory that renders a custom gui, and provides
 * a way to get the {@link CustomGui}.
 */
public record CustomGuiHolder(PaperCustomGui gui) implements InventoryHolder {
    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory = this.gui.getInventory();
        if (inventory == null) {
            throw new IllegalStateException("Custom gui is not rendered.");
        }
        return inventory;
    }
}
