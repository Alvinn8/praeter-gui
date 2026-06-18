package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.CustomGui;
import ca.bkaw.praeter.render.DrawPos;
import ca.bkaw.praeter.render.RenderContext;
import ca.bkaw.praeter.render.State;

public class ExampleGui1 {
	// private static State<Slot> SLOT_1;
	// private static State<DisableableButton> BUTTON;

	public static void setup(RenderContext r) {
		r.useState(ExampleGui1::new);

		// SLOT_1 = Slot.slot(5, 5);
		// BUTTON = DisableableButton.setup(4, 0, 4, 1, "Click me");
		class TempButton { boolean enabled = true; }
		State<TempButton> BUTTON = r.useState(TempButton::new);

		r.renderIf(BUTTON, btn -> btn.enabled, () -> {
			r.drawImage(DrawPos.slotCorner(4, 0), "example:gui/enabled_icon");
		}).elseRender(() -> {
			r.drawImage(DrawPos.slotCorner(4, 0), "example:gui/disabled_icon");
		});
	}

	public ExampleGui1(CustomGui gui) {
		// DisableableButton button = BUTTON.get(gui);
		// button.onClick(ctx -> {
		// 	ctx.playClickSound();
		// 	button.setEnabled(!button.isEnabled());
		// 	ctx.update();
		// });
	}
}
