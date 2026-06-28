package ca.bkaw.praeter.gui.text;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.TestPlatform;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.font.Font;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TextRendererTest {
    @Test
    @Disabled("Uncomment this test to run verify text look correct.")
    public void renderTextHumanTest(@TempDir Path storagePath) throws IOException {
        PraeterGui praeterGui = PraeterGui.bootstrapWithPlatform(new TestPlatform(storagePath));
        praeterGui.setupAssets();

        ResourcePack vanillaAssets = praeterGui.getAssets().getVanillaAssets();
        assertNotNull(vanillaAssets, "Vanilla assets should not be null.");
        Font font = new Font(vanillaAssets, "minecraft:default");

        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        String text = "The quick brown fox jumps over the lazy dog.\n1234567890\n!@#$%^&*()_+-=[]{}|;':\",./<>?";
        TextRenderer.renderText(image, text, 10, 10, java.awt.Color.BLACK, font);

        JFrame frame = new JFrame("TextRenderer Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);

        // Keep the application running to view the buttons
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
