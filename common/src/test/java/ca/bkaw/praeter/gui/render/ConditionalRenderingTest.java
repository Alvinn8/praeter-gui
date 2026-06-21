package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.pack.ResourcePack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConditionalRenderingTest {
    @BeforeAll
    public static void writeTestImages() throws IOException {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, 16, 16);
        graphics.dispose();
        try (OutputStream stream = Files.newOutputStream(Path.of("src/test/resources/pack/assets/test/textures/test1.png"))) {
            ImageIO.write(image, "png", stream);
        }
        try (OutputStream stream = Files.newOutputStream(Path.of("src/test/resources/pack/assets/test/textures/test2.png"))) {
            ImageIO.write(image, "png", stream);
        }
    }

    @Test
    public void testRenderIf() throws IOException {
        ResourcePack pack = ResourcePack.loadDirectory(Path.of("src/test/resources/pack"));
        ResourcePack vanillaAssets = ResourcePack.loadDirectory(Path.of("src/test/resources/vanilla_assets"));

        RenderContextImpl r = new RenderContextImpl(3, pack, vanillaAssets);

        Ref<Boolean> trueRef = r.useState(_ -> true);
        Ref<Boolean> falseRef = r.useState(_ -> false);

        r.renderIf(trueRef, x -> x, () -> r.drawImage(DrawPos.of(0, 0), "test:test1.png"));
        r.renderIf(falseRef, x -> x, () -> r.drawImage(DrawPos.of(0, 0), "test:test2.png"));
    }
}
