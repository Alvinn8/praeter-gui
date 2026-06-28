package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.TestPlatform;
import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiRegistry;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StandaloneRenderTest {
    class TempButton { boolean enabled = true; }
    private static Ref<TempButton> BUTTON;

    @Test
    // @Disabled("Uncomment this test to run verify the rendering looks correct.")
    public void standaloneRenderTest(@TempDir Path storagePath) throws IOException {
        storagePath = Path.of(".test_run");
        PraeterGui.bootstrapWithPlatform(new TestPlatform(storagePath)).setupAssets();

        CustomGuiType type = CustomGuiType.builder()
            .height(1)
            .setup(r -> {
                BUTTON = r.useState(TempButton::new);

                r.renderIf(BUTTON, btn -> btn.enabled, () -> {
                    r.drawImage(DrawPos.slotCorner(SlotPos.of(0, 0)), uniformSquare(Color.GREEN));
                }).elseRender(() -> {
                    r.drawImage(DrawPos.slotCorner(SlotPos.of(0, 0)), uniformSquare(Color.RED));
                });

                Slot.slot(r, SlotPos.of(2, 0));

                Button.button(r, "Click", SlotPos.of(4, 0).cornerPixel(), 3 * 18, 18);
            })
            .build();

        CustomGuiRegistry.register0("example:example1", type);
        CustomGui gui = type.create();

        BufferedImage image = StandaloneRender.render(gui);

        try (OutputStream stream = Files.newOutputStream(storagePath.resolve("standalone_render_test.png"))) {
            ImageIO.write(image, "png", stream);
        }
    }

    private BufferedImage uniformSquare(Color color) {
        BufferedImage image = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, 18, 18);
        graphics.dispose();
        return image;
    }
}
