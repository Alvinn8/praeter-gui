package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.slot.SlotPos;

import java.awt.Color;

import static ca.bkaw.praeter.gui.CommonHooks.drawImage;
import static ca.bkaw.praeter.gui.CommonHooks.renderIf;
import static ca.bkaw.praeter.gui.CommonHooks.useState;

public class ConditionalRenderingExample {
    public static class Counter {
        public int count = 0;
    }

    public static Ref<Counter> COUNTER;

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(1)
        .setup(r -> {
            COUNTER = useState(r, Counter::new);

            Button.button(r, "Click", SlotPos.of(2, 0), 3, 1);

            renderIf(r, COUNTER, counter -> counter.count % 2 == 0, () -> {
                drawImage(r, DrawPos.slotCorner(6, 0), ExampleTextures.icon(18, new Color(46, 125, 50), new Color(102, 187, 106), "E"));
            }).elseRender(() -> {
                drawImage(r, DrawPos.slotCorner(6, 0), ExampleTextures.icon(18, new Color(230, 81, 0), new Color(255, 167, 38), "O"));
            });
        })
        .build();
}
