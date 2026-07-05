package ca.bkaw.praeter.gui.click;

import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.slot.FakeGuiPlayer;
import ca.bkaw.praeter.gui.slot.FakeRenderContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ClickDispatcherTest {
    private CustomGuiType type;
    private CustomGui gui;
    private List<String> fired;

    /**
     * Create a 1-row gui (top slots 0-8, then the player inventory) with:
     * - a global handler that records "global"
     * - a per-slot handler on raw slot 6 that records "slot6"
     * - a 3x1 region (raw slots 2, 3, 4) registered via a single handler that
     *   records "region:&lt;rawSlot&gt;"
     */
    @BeforeEach
    public void setup() {
        this.fired = new ArrayList<>();
        this.type = CustomGuiType.builder()
            .height(1)
            .setup(r -> {})
            .build();
        FakeRenderContext r = new FakeRenderContext();
        r.onClick(ctx -> this.fired.add("global"));
        r.onClick(SlotPos.of(6, 0), ctx -> this.fired.add("slot6"));
        r.onClick(SlotPos.of(2, 0).region(3, 1), ctx -> this.fired.add("region:" + ctx.getRawSlot()));
        this.type.setStateRefs(r.getStateRefs());
        this.type.setClickHandlers(r.getClickHandlers());
        this.type.setSlotClickHandlers(r.getSlotClickHandlers());
        this.gui = new CustomGui(this.type);
    }

    private ClickContext context(int rawSlot) {
        return new FakeClickContext(this.gui, new FakeGuiPlayer("TestPlayer"), rawSlot);
    }

    @Test
    public void globalHandlerFiresForAnyTopSlot() {
        boolean result = ClickDispatcher.fire(this.gui, 0, () -> this.context(0));
        assertTrue(result);
        assertEquals(List.of("global"), this.fired);
    }

    @Test
    public void globalHandlerDoesNotFireOutsideTopGui() {
        // Raw slot 9 is the first player inventory slot in a 1-row gui.
        boolean result = ClickDispatcher.fire(this.gui, 9, () -> fail("Context should not be constructed."));
        assertFalse(result);
        assertTrue(this.fired.isEmpty());
    }

    @Test
    public void noHandlerFiresForInvalidRawSlot() {
        boolean result = ClickDispatcher.fire(this.gui, -999, () -> fail("Context should not be constructed."));
        assertFalse(result);
        assertTrue(this.fired.isEmpty());
    }

    @Test
    public void slotHandlerFiresOnlyForItsOwnSlot() {
        ClickDispatcher.fire(this.gui, 5, () -> this.context(5));
        assertEquals(List.of("global"), this.fired);
    }

    @Test
    public void globalAndSlotHandlerBothFireForSameSlot() {
        ClickDispatcher.fire(this.gui, 6, () -> this.context(6));
        assertEquals(List.of("global", "slot6"), this.fired);
    }

    @Test
    public void regionHandlerFiresIndependentlyForEachSlot() {
        ClickDispatcher.fire(this.gui, 2, () -> this.context(2));
        ClickDispatcher.fire(this.gui, 3, () -> this.context(3));
        assertEquals(List.of("global", "region:2", "global", "region:3"), this.fired);
    }

    private static final class FakeClickContext extends AbstractClickContext {
        private FakeClickContext(CustomGui gui, ca.bkaw.praeter.gui.player.GuiPlayer player, int rawSlot) {
            super(gui, player, rawSlot);
        }

        @Override
        public void playClickSound() {
        }
    }
}
