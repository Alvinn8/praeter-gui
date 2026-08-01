package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.CustomGuiType;

import java.awt.Color;

import static ca.bkaw.praeter.gui.CommonHooks.drawImage;

public class DrawingImagesExample {
    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(3)
        .setup(r -> {
            drawImage(r, DrawPos.of(0, 0),
                ExampleTextures.checkerboard(48, 48, new Color(0, 150, 136), new Color(224, 242, 241), new Color(178, 223, 219)));
        })
        .build();
}
