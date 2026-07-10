package ca.bkaw.praeter.gui.platform;

/**
 * An immutable handle to an item stack.
 * <p>
 * Platforms implement this interface by wrapping their native item stack type.
 * The empty item is represented by the singleton returned from {@link #empty()},
 * so instances of platform implementations always represent at least one item.
 */
public interface GuiItem {
    /**
     * Whether this item is empty.
     *
     * @return Whether this item is empty.
     */
    boolean isEmpty();

    /**
     * Get the number of items in this stack.
     *
     * @return The amount. Zero if this item is empty, otherwise at least 1.
     */
    int getAmount();

    /**
     * Get the maximum number of items that can be stacked in one stack of this item.
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
     * The amount must be at least 1. To represent empty items, use {@link #empty()}.
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
    class Empty implements GuiItem {
        public static final Empty INSTANCE = new Empty();
        private Empty() {}

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