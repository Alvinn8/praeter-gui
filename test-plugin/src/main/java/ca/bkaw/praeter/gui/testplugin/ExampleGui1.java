package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.paper.PaperGuiItem;
import ca.bkaw.praeter.gui.paper.PaperSlotBehavior;
import org.bukkit.Material;

public class ExampleGui1 {
    public static Ref<Slot> SLOT_1;
    public static Ref<Slot> SLOT_2;
    // private static Ref<DisableableButton> BUTTON;

    public static CustomGuiType TYPE = CustomGuiType.builder()
        .height(1)
        .setup(r -> {
            r.useState(ExampleGui1::new);

            // BUTTON = DisableableButton.setup(4, 0, 4, 1, "Click me");
            class TempButton { boolean enabled = true; }
            Ref<TempButton> BUTTON = r.useState(TempButton::new);

            r.renderIf(BUTTON, btn -> btn.enabled, () -> {
                r.drawImage(DrawPos.slotCorner(SlotPos.of(0, 0)), "example:gui/green");
            }).elseRender(() -> {
                r.drawImage(DrawPos.slotCorner(SlotPos.of(0, 0)), "example:gui/red");
            });

            SLOT_1 = Slot.slot(r, SlotPos.of(2, 0));
            SLOT_2 = Slot.slot(r, SlotPos.of(3, 0),
                PaperSlotBehavior.of(item -> item.getType() == Material.DIAMOND));

            // Display the item in slot 1, mirrored, as a decorative item that
            // cannot be taken.
            r.renderItem(SlotPos.of(8, 0), PaperGuiItem.renderer(gui ->
                PaperGuiItem.toItemStack(SLOT_1.get(gui).getItem())
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
