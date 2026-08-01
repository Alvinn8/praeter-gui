package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiRegistry;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.render.StandaloneRender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageGen {
    public static final Logger LOGGER = LoggerFactory.getLogger("ImageGen");

    public static void main(String[] args) throws IOException {
        Path storagePath = Path.of("run");
        PraeterGui.bootstrapWithPlatform(new ImageGenPlatform(storagePath));

        Path workDir = Path.of("").toAbsolutePath();
        Path projectRoot = workDir.getFileName().toString().equals("docs-imagegen") ? workDir.getParent() : workDir;
        Path assets = projectRoot.resolve("docs/src/assets");
        LOGGER.info("Saving images to {}", assets.normalize().toAbsolutePath());

        render(GettingStarted.TYPE, "docs:getting_started", assets.resolve("getting_started.png"));
        render(DrawingImagesExample.TYPE, "docs:drawing_images", assets.resolve("gui_basics_drawing_images.png"));
        render(UsingComponentsExample.TYPE, "docs:using_components", assets.resolve("gui_basics_using_components.png"));
        renderConditionalRendering(assets);
    }

    public static void render(CustomGuiType guiType, String id, Path path) throws IOException {
        CustomGuiRegistry.register(id, guiType);
        CustomGui gui = guiType.create();
        save(StandaloneRender.render(gui), path);
    }

    private static void renderConditionalRendering(Path assets) throws IOException {
        CustomGuiRegistry.register("docs:conditional_rendering", ConditionalRenderingExample.TYPE);
        CustomGui gui = ConditionalRenderingExample.TYPE.create();

        // count starts at 0, which is even
        save(StandaloneRender.render(gui), assets.resolve("gui_basics_conditional_even.png"));

        ConditionalRenderingExample.COUNTER.get(gui).count++;
        save(StandaloneRender.render(gui), assets.resolve("gui_basics_conditional_odd.png"));
    }

    private static void save(BufferedImage image, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (OutputStream stream = Files.newOutputStream(path)) {
            ImageIO.write(image, "png", stream);
        }
    }
}
