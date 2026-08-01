package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Panel;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.slot.SlotPos;

public class UsingComponentsExample {
    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(3)
        .setup(r -> {
            Button.button(r, "Click", SlotPos.of(2, 0), 3, 1);
            Panel.panel(r, DrawPos.slotCorner(0, 1), 4 * DrawPos.SLOT_SIZE, 2 * DrawPos.SLOT_SIZE);
        })
        .build();
}
