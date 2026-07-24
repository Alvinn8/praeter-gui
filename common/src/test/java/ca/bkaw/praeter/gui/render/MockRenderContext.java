package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.CommonHooks;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.StateRefImpl;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import ca.bkaw.praeter.gui.slot.ItemRenderer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A render context for testing that does not perform any rendering.
 */
public class MockRenderContext implements RenderContext {
    private final List<StateRefImpl<?>> stateRefs = new ArrayList<>();
    private final List<GuiSlot> guiSlots = new ArrayList<>();

    public List<StateRefImpl<?>> getStateRefs() {
        return this.stateRefs;
    }

    public List<GuiSlot> getGuiSlots() {
        return this.guiSlots;
    }

    @Override
    public void addStateRef(StateRefImpl<?> stateRef) {
        this.stateRefs.add(stateRef);
    }

    @Override
    public void drawImage(DrawPos pos, String textureIdentifier) {}

    @Override
    public void drawImage(DrawPos pos, BufferedImage image) {}

    @Override
    public void addRenderStep(RenderStep step) {}

    @Override
    public void addSlot(GuiSlot guiSlot) {
        this.guiSlots.add(guiSlot);
    }

    @Override
    public void addItemRenderer(ItemRenderer itemRenderer) {}

    @Override
    public CommonHooks.RenderIf renderIf(Predicate<CustomGui> condition, Runnable renderer) {
        renderer.run();
        return new CommonHooks.RenderIf() {
            @Override
            public CommonHooks.RenderIf elseIf(Predicate<CustomGui> condition, Runnable renderer) {
                renderer.run();
                return this;
            }

            @Override
            public void elseRender(Runnable renderer) {
                renderer.run();
            }
        };
    }

    @Override
    public void addCreatedListener(Consumer<CustomGui> action) {

    }
}
