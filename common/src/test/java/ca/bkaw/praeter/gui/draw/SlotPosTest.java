package ca.bkaw.praeter.gui.draw;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlotPosTest {
    @Test
    public void regionEnumeratesSlotsRowByRow() {
        List<Integer> rawSlots = SlotPos.of(4, 0).region(3, 2).stream()
            .map(SlotPos::slotIndex)
            .toList();
        // Starting at slot (4, 0) in a 9-wide grid: a 3x2 region covers
        // (4,0) (5,0) (6,0) then (4,1) (5,1) (6,1).
        assertEquals(List.of(4, 5, 6, 13, 14, 15), rawSlots);
    }
}
