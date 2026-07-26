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

        Path workDir = Path.of(".");
        Path projectRoot = workDir.getFileName().toString().equals("docs-imagegen") ? Path.of("..") : workDir;
        Path assets = projectRoot.resolve("docs/src/assets");
        LOGGER.info("Saving images to {}", assets.normalize().toAbsolutePath());

        render(GettingStarted.TYPE, "docs:getting_started", assets.resolve("getting_started.png"));
    }

    public static void render(CustomGuiType guiType, String id, Path path) throws IOException {
        CustomGuiRegistry.register0(id, guiType);
        CustomGui gui = guiType.create();
        BufferedImage image = StandaloneRender.render(gui);
        Files.createDirectories(path.getParent());
        try (OutputStream stream = Files.newOutputStream(path)) {
            ImageIO.write(image, "png", stream);
        }
    }
}
