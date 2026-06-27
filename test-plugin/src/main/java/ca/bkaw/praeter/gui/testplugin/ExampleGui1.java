package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.render.DrawPos;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.render.Ref;

public class ExampleGui1 {
    // private static Ref<Slot> SLOT_1;
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
                r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/green");
            }).elseRender(() -> {
                r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/red");
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
