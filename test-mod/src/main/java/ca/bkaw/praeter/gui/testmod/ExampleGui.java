package ca.bkaw.praeter.gui.testmod;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.slot.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGuiType;

import static ca.bkaw.praeter.gui.CommonHooks.useTitle;

public final class ExampleGui {
    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(1)
        .setup(r -> {
            useTitle(r, "Example GUI");
            Slot.slot(r, SlotPos.of(0, 0));
            Button.button(r, "Click", SlotPos.of(2, 0), 3, 1);
        })
        .build();

    private ExampleGui() {}
}
