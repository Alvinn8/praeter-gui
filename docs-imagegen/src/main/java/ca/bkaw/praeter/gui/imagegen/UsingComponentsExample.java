package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Panel;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.slot.SlotPos;

import static ca.bkaw.praeter.gui.CommonHooks.useTitle;

public class UsingComponentsExample {
    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(3)
        .setup(r -> {
            useTitle(r, "My GUI");
            Button.button(r, "Click", SlotPos.of(5, 1), 3, 1);
            Panel.panel(r, DrawPos.slotCorner(0, 0), 4 * DrawPos.SLOT_SIZE, 3 * DrawPos.SLOT_SIZE);
        })
        .build();
}
