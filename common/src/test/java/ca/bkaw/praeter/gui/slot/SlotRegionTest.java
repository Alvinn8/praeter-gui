package ca.bkaw.praeter.gui.slot;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlotRegionTest {
    @Test
    public void iteratorTest() {
        SlotRegion region = new SlotRegion() {
            @Override
            public int getSlotX() {
                return 1;
            }

            @Override
            public int getSlotY() {
                return 2;
            }

            @Override
            public int getSlotWidth() {
                return 2;
            }

            @Override
            public int getSlotHeight() {
                return 2;
            }
        };

        List<SlotPos> expected = List.of(
            SlotPos.of(1, 2),
            SlotPos.of(2, 2),
            SlotPos.of(1, 3),
            SlotPos.of(2, 3)
        );

        List<SlotPos> actual = new ArrayList<>(List.of());
        for (SlotPos pos : region) {
            actual.add(pos);
        }

        assertEquals(expected, actual);
    }
}
