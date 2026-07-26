package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.slot.SlotPos;

public class GettingStarted {
    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(1)
        .setup(r -> {
            Slot.slot(r, SlotPos.of(0, 0));
            Button.button(r, "Click", SlotPos.of(2, 0), 3, 1);
        })
        .build();
}
