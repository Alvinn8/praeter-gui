package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.slot.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.Ref;
import org.bukkit.inventory.ItemStack;

import static ca.bkaw.praeter.gui.CommonHooks.*;
import static ca.bkaw.praeter.gui.paper.PaperHooks.*;

public class ExampleGui1 {
    private static Ref<Slot> SLOT_1;
    private static Ref<Button> BUTTON_2;

    public static CustomGuiType TYPE = CustomGuiType.builder()
        .height(1)
        .setup(r -> {
            useState(r, ExampleGui1::new);

            // SLOT_1 = Slot.slot(5, 5);
            // BUTTON = DisableableButton.setup(4, 0, 4, 1, "Click me");
            class TempButton { boolean enabled = true; }
            Ref<TempButton> BUTTON = useState(r, TempButton::new);

            renderIf(r, BUTTON, btn -> btn.enabled, () -> {
                r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/green");
            }).elseRender(() -> {
                r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/red");
            });

            SLOT_1 = Slot.slot(r, SlotPos.of(2, 0));

            // Copy the item from the slot and show at the end.
            renderGuiItem(r, SlotPos.of(8, 0), gui -> SLOT_1.get(gui).getGuiItem());

            BUTTON_2 = Button.button(r, "Click", SlotPos.of(4, 0), 3, 1);
            onClick(r, BUTTON_2, ctx -> {
                ItemStack item = getSlotItem(SLOT_1, ctx.getGui());
                if (item != null) {
                    item.add();
                    setSlotItem(SLOT_1, ctx.getGui(), item);
                    ctx.playClickSound();
                    ctx.getGui().update();
                }
            });
        })
        .build();

    public ExampleGui1(CustomGui gui) {
        // DisableableButton button = BUTTON.get(gui);
        // button.onClick(ctx -> {
        //     ctx.playClickSound();
        //     button.setEnabled(!button.isEnabled());
        //     ctx.update();
        // });
    }
}
