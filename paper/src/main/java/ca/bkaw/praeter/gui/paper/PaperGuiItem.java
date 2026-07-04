package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.item.GuiItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A {@link GuiItem} that wraps a Bukkit {@link ItemStack}.
 * <p>
 * The wrapped item stack is never air and always has an amount of at least 1.
 * Empty items are represented by {@link GuiItem#empty()}.
 */
public final class PaperGuiItem implements GuiItem {
    private final ItemStack itemStack;

    private PaperGuiItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Create a {@link GuiItem} from an item stack.
     *
     * @param itemStack The item stack. Null and air are treated as the empty item.
     * @return The gui item.
     */
    public static GuiItem of(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() <= 0) {
            return GuiItem.empty();
        }
        return new PaperGuiItem(itemStack.clone());
    }

    /**
     * Wrap a function that computes an item stack from a gui instance so that it
     * can be used with
     * {@link ca.bkaw.praeter.gui.CommonHooks#renderItem CommonHooks.renderItem}.
     * <p>
     * Most users use {@link PaperHooks#renderItemStack} which wraps the function
     * automatically.
     *
     * @param function The function computing the item stack to display. May return
     *                 null to display nothing.
     * @return The wrapped function.
     */
    public static Function<CustomGui, GuiItem> renderer(Function<CustomGui, @Nullable ItemStack> function) {
        return gui -> of(function.apply(gui));
    }

    /**
     * Convert a {@link GuiItem} to an item stack.
     *
     * @param item The gui item.
     * @return The item stack, or null if the item is empty.
     */
    public static @Nullable ItemStack toItemStack(GuiItem item) {
        if (item.isEmpty()) {
            return null;
        }
        return ((PaperGuiItem) item).itemStack.clone();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getAmount() {
        return this.itemStack.getAmount();
    }

    @Override
    public int getMaxStackSize() {
        return this.itemStack.getMaxStackSize();
    }

    @Override
    public boolean canStackWith(GuiItem other) {
        return other instanceof PaperGuiItem paperItem
            && this.itemStack.isSimilar(paperItem.itemStack);
    }

    @Override
    public GuiItem withAmount(int amount) {
        ItemStack copy = this.itemStack.clone();
        copy.setAmount(amount);
        return new PaperGuiItem(copy);
    }

    @Override
    public GuiItem copy() {
        return new PaperGuiItem(this.itemStack.clone());
    }
}
