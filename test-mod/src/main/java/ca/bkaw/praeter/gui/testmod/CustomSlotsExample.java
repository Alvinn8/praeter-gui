package ca.bkaw.praeter.gui.testmod;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.slot.SlotPos;
import net.minecraft.world.item.Items;

import static ca.bkaw.praeter.gui.CommonHooks.*;
import static ca.bkaw.praeter.gui.fabric.FabricHooks.*;

public class CustomSlotsExample {
    public static CustomGuiType TYPE = CustomGuiType.builder()
        .setup(r -> {
            Slot.slot(r, SlotPos.of(0, 0), slotCanHold((gui, item) -> item.is(Items.DIAMOND)));
            Slot.slot(r, SlotPos.of(1, 0), slotMayChange((gui, player) -> player.isCreative()));
        })
        .build();
}
