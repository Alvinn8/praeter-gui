package ca.bkaw.praeter.gui.item;

/**
 * A platform-agnostic, immutable handle to an item stack.
 * <p>
 * Platforms implement this interface by wrapping their native item stack type.
 * The empty item is represented by the singleton returned from {@link #empty()},
 * so instances of platform implementations always represent at least one item.
 * <p>
 * Instances must be treated as immutable. Methods that would modify the item,
 * like {@link #withAmount(int)}, return a new instance.
 */
public interface GuiItem {

    /**
     * Whether this item is empty. Only the {@link #empty()} singleton is empty,
     * so this is equivalent to {@code this == GuiItem.empty()}.
     *
     * @return Whether this item is empty.
     */
    boolean isEmpty();

    /**
     * Get the amount of items in this stack.
     *
     * @return The amount. Zero if this item is empty, otherwise at least 1.
     */
    int getAmount();

    /**
     * Get the maximum amount of items that can be stacked in one stack of this item.
     *
     * @return The maximum stack size.
     */
    int getMaxStackSize();

    /**
     * Whether this item can stack together with the other item. Two items can stack
     * when they are the same item with the same metadata, regardless of the amounts
     * of the two stacks.
     * <p>
     * The empty item can never stack with anything.
     *
     * @param other The other item.
     * @return Whether the items can stack.
     */
    boolean canStackWith(GuiItem other);

    /**
     * Create a copy of this item with the given amount.
     * <p>
     * The amount must be at least 1. To represent zero items, use {@link #empty()}.
     *
     * @param amount The amount of the new item. At least 1.
     * @return The new item.
     */
    GuiItem withAmount(int amount);

    /**
     * Create a copy of this item.
     *
     * @return The copy.
     */
    GuiItem copy();

    /**
     * Get the empty item, representing the absence of an item.
     *
     * @return The empty item singleton.
     */
    static GuiItem empty() {
        return Empty.INSTANCE;
    }

    /**
     * The singleton implementation of the empty item.
     */
    enum Empty implements GuiItem {
        INSTANCE;

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int getAmount() {
            return 0;
        }

        @Override
        public int getMaxStackSize() {
            return 0;
        }

        @Override
        public boolean canStackWith(GuiItem other) {
            return false;
        }

        @Override
        public GuiItem withAmount(int amount) {
            throw new UnsupportedOperationException("Cannot change the amount of the empty item.");
        }

        @Override
        public GuiItem copy() {
            return this;
        }
    }
}
