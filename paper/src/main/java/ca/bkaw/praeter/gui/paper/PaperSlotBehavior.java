package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.player.GuiPlayer;
import ca.bkaw.praeter.gui.slot.SlotBehavior;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

/**
 * A {@link SlotBehavior} that is specified using Bukkit types instead of the
 * platform-agnostic types.
 * <p>
 * Since {@link #mayChange(HumanEntity)} has a default implementation, a behavior
 * that only restricts items can be created from a lambda with {@link #of}:
 * <pre>
 * Slot.slot(r, pos, PaperSlotBehavior.of(item -&gt; item.getType() == Material.DIAMOND));
 * </pre>
 */
@FunctionalInterface
public interface PaperSlotBehavior extends SlotBehavior {

    /**
     * Whether the slot can hold the given item.
     *
     * @param itemStack The item stack. Never null or air.
     * @return Whether the slot can hold the item.
     */
    boolean canHold(ItemStack itemStack);

    /**
     * Whether the given player may change the contents of the slot.
     * <p>
     * Returns true by default.
     *
     * @param player The player.
     * @return Whether the player may change the slot.
     */
    default boolean mayChange(HumanEntity player) {
        return true;
    }

    @Override
    default boolean canHold(GuiItem item) {
        ItemStack itemStack = PaperGuiItem.toItemStack(item);
        return itemStack != null && this.canHold(itemStack);
    }

    @Override
    default boolean mayChange(GuiPlayer player) {
        return this.mayChange(((PaperGuiPlayer) player).getHumanEntity());
    }

    /**
     * Create a behavior that only restricts which items the slot can hold.
     *
     * @param canHold The predicate deciding which items the slot can hold.
     * @return The behavior.
     */
    static PaperSlotBehavior of(Predicate<ItemStack> canHold) {
        return canHold::test;
    }
}
