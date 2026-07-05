package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.paper.PaperSlotBehavior;
import org.bukkit.Material;

import static ca.bkaw.praeter.gui.CommonHooks.*;
import static ca.bkaw.praeter.gui.paper.PaperHooks.*;

public class ExampleGui1 {
    public static Ref<Slot> SLOT_1;
    public static Ref<Slot> SLOT_2;

    public static CustomGuiType TYPE = CustomGuiType.builder()
        .height(1)
        .setup(r -> {
            useState(r, ExampleGui1::new);

            class TempButton { boolean enabled = true; }
            Ref<TempButton> BUTTON = useState(r, TempButton::new);

            renderIf(r, BUTTON, btn -> btn.enabled, () -> {
                drawImage(r, DrawPos.slotCorner(SlotPos.of(0, 0)), "example:gui/green");
            }).elseRender(() -> {
                drawImage(r, DrawPos.slotCorner(SlotPos.of(0, 0)), "example:gui/red");
            });

            SLOT_1 = Slot.slot(r, SlotPos.of(2, 0));
            SLOT_2 = Slot.slot(r, SlotPos.of(3, 0),
                PaperSlotBehavior.of(item -> item.getType() == Material.DIAMOND));

            // Display the item in slot 1, mirrored, as a decorative item that
            // cannot be taken.
            renderItemStack(r, SlotPos.of(8, 0), gui -> getSlotItem(SLOT_1, gui));

            hoverText(r, SlotPos.of(1, 0),
                "Example gui", "Hover text with", "multiple lines.");

            Button.button(r, "Click", SlotPos.of(4, 0), 3, 1, ctx -> {
                ctx.playClickSound();
                TempButton button = BUTTON.get(ctx.getGui());
                button.enabled = !button.enabled;
            });
        })
        .build();

    public ExampleGui1(CustomGui gui) {
    }
}
