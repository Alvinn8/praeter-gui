package ca.bkaw.praeter.gui.draw;

import java.util.ArrayList;
import java.util.List;

/**
 * A position of a slot in a gui.
 */
public interface SlotPos {
    /**
     * Get the slot index used by the game for this slot.
     *
     * @return The slot index.
     */
    int slotIndex();

    /**
     * Get the top-left corner pixel of the slot.
     */
    DrawPos cornerPixel();

    /**
     * Get the raw slot positions of a rectangular region of a 9xN grid, with this
     * slot as the top-left corner.
     *
     * @param widthInSlots The width of the region, in slots.
     * @param heightInSlots The height of the region, in slots.
     * @return The slot positions in the region.
     */
    default List<SlotPos> region(int widthInSlots, int heightInSlots) {
        int slotX = this.slotIndex() % 9;
        int slotY = this.slotIndex() / 9;
        List<SlotPos> result = new ArrayList<>(widthInSlots * heightInSlots);
        for (int dy = 0; dy < heightInSlots; dy++) {
            for (int dx = 0; dx < widthInSlots; dx++) {
                result.add(SlotPos.of(slotX + dx, slotY + dy));
            }
        }
        return result;
    }

    /**
     * Create a {@link SlotPos} from the given slot index.
     *
     * @param slotIndex The index of the slot.
     * @return A {@link SlotPos} representing the slot at the given index.
     */
    static SlotPos of(int slotIndex) {
        return new GenricContainer(slotIndex);
    }

    /**
     * Create a {@link SlotPos} from the given slot coordinates in a 9xN grid.
     *
     * @param slotX The x coordinate of the slot [0-8].
     * @param slotY The y coordinate of the slot.
     * @return A {@link SlotPos} representing the slot at the given coordinates.
     */
    static SlotPos of(int slotX, int slotY) {
        return new GenricContainer(slotY * 9 + slotX);
    }

    /**
     * A slot in a generic container with a 9xN grid.
     *
     * @param slotIndex The index of the slot in the container.
     */
    record GenricContainer(int slotIndex) implements SlotPos {
        @Override
        public DrawPos cornerPixel() {
            return DrawPos.slotOrigin().add(
                (this.slotIndex % 9) * DrawPos.SLOT_SIZE,
                (this.slotIndex / 9) * DrawPos.SLOT_SIZE
            );
        }
    }
}
