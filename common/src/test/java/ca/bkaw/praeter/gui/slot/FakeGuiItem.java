package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.item.GuiItem;

/**
 * A fake item for testing the slot interaction handler without a platform.
 *
 * @param type An identifier for the item type. Items with the same type can stack.
 * @param amount The amount of items in the stack.
 * @param maxStackSize The maximum stack size.
 */
public record FakeGuiItem(String type, int amount, int maxStackSize) implements GuiItem {

    public static FakeGuiItem of(String type, int amount) {
        return new FakeGuiItem(type, amount, 64);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    @Override
    public boolean canStackWith(GuiItem other) {
        return other instanceof FakeGuiItem fake && fake.type.equals(this.type);
    }

    @Override
    public GuiItem withAmount(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must be at least 1, was " + amount);
        }
        return new FakeGuiItem(this.type, amount, this.maxStackSize);
    }

    @Override
    public GuiItem copy() {
        return this;
    }
}
