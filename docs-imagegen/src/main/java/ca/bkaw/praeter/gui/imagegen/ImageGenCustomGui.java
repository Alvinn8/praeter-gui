package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ImageGenCustomGui extends CustomGui {
    private @Nullable Function<CustomGui, String> titleFunction;

    public ImageGenCustomGui(CustomGuiType type) {
        super(type);
    }

    @Override
    public void update() {}

    public @Nullable Function<CustomGui, String> getTitleFunction() {
        return this.titleFunction;
    }

    public void setTitleFunction(@Nullable Function<CustomGui, String> titleFunction) {
        this.titleFunction = titleFunction;
    }
}
