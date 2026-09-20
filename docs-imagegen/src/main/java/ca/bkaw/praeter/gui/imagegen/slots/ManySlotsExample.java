package ca.bkaw.praeter.gui.imagegen.slots;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.slot.SlotPos;

import java.util.ArrayList;
import java.util.List;

import static ca.bkaw.praeter.gui.CommonHooks.useTitle;

public class ManySlotsExample {
    private static List<Ref<Slot>> LEFT_SLOTS;
    private static List<Ref<Slot>> RIGHT_SLOTS;

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(6)
        .setup(r -> {
            useTitle(r, "A lot of slots.");
            LEFT_SLOTS = new ArrayList<>();
            RIGHT_SLOTS = new ArrayList<>();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 6; y++) {
                    LEFT_SLOTS.add(Slot.slot(r, SlotPos.of(x, y)));
                    RIGHT_SLOTS.add(Slot.slot(r, SlotPos.of(x + 6, y)));
                }
            }
        })
        .build();
}