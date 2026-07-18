package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.slot.ItemRenderer;

public class ExampleGui1 {
    private static Ref<Slot> SLOT_1;
    // private static Ref<DisableableButton> BUTTON;

    public static CustomGuiType TYPE = CustomGuiType.builder()
        .height(1)
        .setup(r -> {
            r.useState(ExampleGui1::new);

            // SLOT_1 = Slot.slot(5, 5);
            // BUTTON = DisableableButton.setup(4, 0, 4, 1, "Click me");
            class TempButton { boolean enabled = true; }
            Ref<TempButton> BUTTON = r.useState(TempButton::new);

            r.renderIf(BUTTON, btn -> btn.enabled, () -> {
                r.drawImage(DrawPos.slotCorner(SlotPos.of(0, 0)), "example:gui/green");
            }).elseRender(() -> {
                r.drawImage(DrawPos.slotCorner(SlotPos.of(0, 0)), "example:gui/red");
            });

            SLOT_1 = Slot.slot(r, SlotPos.of(2, 0));

            // Copy the item from the slot and show at the end.
            r.addItemRenderer(new ItemRenderer(SlotPos.of(8, 0).slotIndex(), (gui) ->
                SLOT_1.get(gui).getGuiItem()
            ));

            Button.button(r, "Click", SlotPos.of(4, 0).cornerPixel(), 3 * 18, 18);
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
