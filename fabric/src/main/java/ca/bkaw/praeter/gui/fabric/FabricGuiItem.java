package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.item.GuiItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link GuiItem} that wraps a Minecraft {@link ItemStack}.
 * <p>
 * The wrapped item stack is never empty. Empty items are represented by
 * {@link GuiItem#empty()}.
 */
public final class FabricGuiItem implements GuiItem {
    private final ItemStack itemStack;

    private FabricGuiItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Create a {@link GuiItem} from an item stack.
     *
     * @param itemStack The item stack. Null and empty are treated as the empty item.
     * @return The gui item.
     */
    public static GuiItem of(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return GuiItem.empty();
        }
        return new FabricGuiItem(itemStack.copy());
    }

    /**
     * Convert a {@link GuiItem} to an item stack.
     *
     * @param item The gui item.
     * @return The item stack, or {@link ItemStack#EMPTY} if the item is empty.
     */
    public static ItemStack toItemStack(GuiItem item) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ((FabricGuiItem) item).itemStack.copy();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getAmount() {
        return this.itemStack.getCount();
    }

    @Override
    public int getMaxStackSize() {
        return this.itemStack.getMaxStackSize();
    }

    @Override
    public boolean canStackWith(GuiItem other) {
        return other instanceof FabricGuiItem fabricItem
            && ItemStack.isSameItemSameComponents(this.itemStack, fabricItem.itemStack);
    }

    @Override
    public GuiItem withAmount(int amount) {
        return new FabricGuiItem(this.itemStack.copyWithCount(amount));
    }

    @Override
    public GuiItem copy() {
        return new FabricGuiItem(this.itemStack.copy());
    }
}
