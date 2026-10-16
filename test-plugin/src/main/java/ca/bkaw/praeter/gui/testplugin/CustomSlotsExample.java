package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.slot.SlotPos;
import org.bukkit.GameMode;
import org.bukkit.Material;

import static ca.bkaw.praeter.gui.CommonHooks.*;
import static ca.bkaw.praeter.gui.paper.PaperHooks.*;

public class CustomSlotsExample {
    public static CustomGuiType TYPE = CustomGuiType.builder()
        .setup(r -> {
            Slot.slot(r, SlotPos.of(0, 0), slotCanHold((gui, item) -> item.getType() == Material.DIAMOND));
            Slot.slot(r, SlotPos.of(1, 0), slotMayChange((gui, player) -> player.getGameMode() == GameMode.CREATIVE));
        })
        .build();
}
