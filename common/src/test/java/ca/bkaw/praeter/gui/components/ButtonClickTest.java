package ca.bkaw.praeter.gui.components;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.TestPlatform;
import ca.bkaw.praeter.gui.click.ClickDispatcher;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiRegistry;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ButtonClickTest {
    @Test
    public void buttonRegionOnlyFiresOnItsOwnSlots(@TempDir Path storagePath) {
        PraeterGui.bootstrapWithPlatform(new TestPlatform(storagePath)).setupAssets();

        List<Integer> clicked = new ArrayList<>();
        CustomGuiType type = CustomGuiType.builder()
            .height(1)
            .setup(r -> Button.button(r, "Click", SlotPos.of(4, 0), 3, 1,
                ctx -> clicked.add(ctx.getRawSlot())))
            .build();
        CustomGuiRegistry.register0("example:button_click_test", type);
        CustomGui gui = type.create();

        // The button occupies raw slots 4, 5 and 6.
        for (int rawSlot : List.of(4, 5, 6)) {
            boolean fired = ClickDispatcher.fire(gui, rawSlot,
                () -> new FakeClickContext(gui, rawSlot));
            assertTrue(fired, "Expected a click on raw slot " + rawSlot + " to fire.");
        }
        assertEquals(List.of(4, 5, 6), clicked);

        clicked.clear();
        for (int rawSlot : List.of(3, 7)) {
            boolean fired = ClickDispatcher.fire(gui, rawSlot,
                () -> new FakeClickContext(gui, rawSlot));
            assertFalse(fired, "Did not expect a click on raw slot " + rawSlot + " to fire.");
        }
        assertTrue(clicked.isEmpty());
    }

    private static final class FakeClickContext extends ca.bkaw.praeter.gui.click.AbstractClickContext {
        private FakeClickContext(CustomGui gui, int rawSlot) {
            super(gui, new ca.bkaw.praeter.gui.slot.FakeGuiPlayer("TestPlayer"), rawSlot);
        }

        @Override
        public void playClickSound() {
        }
    }
}
