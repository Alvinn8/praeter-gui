package ca.bkaw.praeter.gui.webtest;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiRegistry;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.render.StandaloneRender;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Entry point for the CheerpJ web test.
 * <p>
 * Called from JavaScript via CheerpJ's library mode:
 * <pre>
 *   const lib = await cheerpjRunLibrary("/app/praeter-web-test.jar");
 *   const WebTestMain = await lib.ca.bkaw.praeter.gui.webtest.WebTestMain;
 *   const base64 = await WebTestMain.renderToBase64();
 *   document.getElementById("preview").src = "data:image/png;base64," + base64;
 * </pre>
 */
public class WebTestMain {

    // State class for the conditional rendering test
    static class ToggleState {
        boolean on = true;
    }

    /**
     * Bootstraps praeter-gui, renders a test GUI, and returns the result as a
     * base64-encoded PNG string suitable for use in a data: URI.
     * <p>
     * On first call, vanilla assets (~34 MB Minecraft client JAR) will be downloaded
     * from Mojang and cached at the storage path. Subsequent calls are fast.
     */
    /**
     * Like {@link #renderToBase64()} but catches all exceptions and returns them as
     * a diagnostic string prefixed with "ERROR:" so the browser can display them
     * without CheerpJ's exception-to-JS translation swallowing the details.
     */
    public static String renderToBase64Safe() {
        try {
            return renderToBase64();
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return "ERROR:" + t.getClass().getName() + ": " + t.getMessage() + "\n" + sw;
        }
    }

    public static String renderToBase64() throws Exception {
        // CheerpJ maps /files/ to IndexedDB-backed persistent storage.
        // Vanilla assets are downloaded once and cached here.
        Path storagePath = Path.of("/files/.praeter_gui");

        PraeterGui praeterGui = PraeterGui.bootstrapWithPlatform(new WebTestPlatform(storagePath))
            .skipSender(); // no TCP resource pack sender needed in browser
        praeterGui.setupAssets(); // downloads vanilla assets on first run (~34 MB)

        // Define a simple test GUI with a conditional element
        Ref<ToggleState>[] toggleRef = new Ref[1];
        CustomGuiType type = CustomGuiType.builder()
            .height(3)
            .setup(r -> {
                toggleRef[0] = r.useState(ToggleState::new);

                // Static colored squares baked into the background
                r.drawImage(DrawPos.slotCorner(SlotPos.of(0, 0)), solidSquare(Color.GREEN, 18, 18));
                r.drawImage(DrawPos.slotCorner(SlotPos.of(2, 0)), solidSquare(Color.BLUE, 36, 18));
                r.drawImage(DrawPos.slotCorner(SlotPos.of(5, 0)), solidSquare(Color.RED, 18, 18));

                // Conditional element: drawn as a font sequence, not baked into background
                r.renderIf(toggleRef[0], s -> s.on, () ->
                    r.drawImage(DrawPos.slotCorner(SlotPos.of(8, 0)), solidSquare(Color.YELLOW, 18, 18))
                ).elseRender(() ->
                    r.drawImage(DrawPos.slotCorner(SlotPos.of(8, 0)), solidSquare(Color.DARK_GRAY, 18, 18))
                );
            })
            .build();

        CustomGuiRegistry.register0("webtest:test1", type);
        CustomGui gui = type.create();

        // Render with the toggle ON
        BufferedImage image = StandaloneRender.render(gui);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /** Standalone entry point for testing without CheerpJ (runs on the JVM). */
    public static void main(String[] args) throws Exception {
        System.out.println("Rendering test GUI...");
        String base64 = renderToBase64();
        System.out.println("Success! Base64 length: " + base64.length());
        System.out.println("Paste into browser: data:image/png;base64," + base64.substring(0, 80) + "...");
    }

    private static BufferedImage solidSquare(Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }
}
