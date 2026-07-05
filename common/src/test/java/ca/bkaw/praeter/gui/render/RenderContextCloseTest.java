package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class RenderContextCloseTest {

    @Test
    public void closedContextThrows(@TempDir Path tempDir) throws IOException {
        ResourcePack pack = ResourcePack.loadDirectory(tempDir);
        ResourcePack vanillaAssets = ResourcePack.loadDirectory(Path.of("src/test/resources/vanilla_assets"));
        RenderContextImpl r = new RenderContextImpl(1, pack, vanillaAssets);

        // Works while open.
        var ref = r.useState(() -> 0);

        r.close();

        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        assertThrows(IllegalStateException.class, () -> r.useState(() -> 0));
        assertThrows(IllegalStateException.class, () -> r.drawImage(DrawPos.of(0, 0), image));
        assertThrows(IllegalStateException.class, () -> r.renderIf(ref, i -> true, () -> {}));
        assertThrows(IllegalStateException.class, () -> r.onClick(ctx -> {}));
        assertThrows(IllegalStateException.class, () -> r.onClick(SlotPos.of(0), ctx -> {}));
    }
}
