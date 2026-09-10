package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.slot.SlotPos;

import static ca.bkaw.praeter.gui.CommonHooks.useTitle;

public class SlotsExample {
    private static Ref<Slot> SLOT_A;
    private static Ref<Slot> SLOT_B;
    private static Ref<Slot> SLOT_C;

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(3)
        .setup(r -> {
            useTitle(r, "Slots");
            SLOT_A = Slot.slot(r, SlotPos.of(1, 1));
            SLOT_B = Slot.slot(r, SlotPos.of(3, 1));
            SLOT_C = Slot.slot(r, SlotPos.of(6, 1));
        })
        .build();
}
