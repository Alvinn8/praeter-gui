package ca.bkaw.praeter.gui.components;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.TestPlatform;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

public class ButtonTest {
    @Test
    @Disabled("Uncomment this test to run verify buttons look correct.")
    public void varyingSizeHumanTest(@TempDir Path storagePath) {
        PraeterGui.bootstrapWithPlatform(new TestPlatform(storagePath)).setupAssets();

        List<Integer> sizes = List.of(10, 18, 20, 30, 50, 100, 200, 500);

        JFrame frame = new JFrame("Button Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);

        for (int size : sizes) {
            BufferedImage image = Button.createButtonImage(size, size);
            String label = size + "x" + size;
            panel.add(new JLabel(label, new ImageIcon(image), JLabel.CENTER));
        }

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
