package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.gui.StateRefImpl;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import ca.bkaw.praeter.gui.slot.ItemRenderer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
    public <T> Ref<T> useState(Function<CustomGui, T> initializer) {
        StateRefImpl<T> ref = new StateRefImpl<>(initializer);
        this.stateRefs.add(ref);
        return ref;
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
    public <T> RenderIf renderIf(Ref<T> ref, Predicate<T> condition, Runnable renderer) {
        renderer.run();
        return new RenderIf() {
            @Override
            public <U> RenderIf elseIf(Ref<U> ref, Predicate<U> condition, Runnable renderer) {
                renderer.run();
                return this;
            }

            @Override
            public void elseRender(Runnable renderer) {
                renderer.run();
            }
        };
    }
}
