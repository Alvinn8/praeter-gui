package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.gui.CustomGui;

import java.util.List;

/**
 * Utilities for testing rendering behaviour.
 */
public class RenderTestUtils {

    /**
     * Create a render step that adds the given event to the list when it is executed
     * during rendering.
     *
     * @param events The list to add the event to when the step is executed.
     * @param event  The event to record.
     * @param <T>    The event type.
     * @return A render step that records the event on execution.
     */
    public static <T> RenderStep tracking(List<T> events, T event) {
        return (_, _) -> events.add(event);
    }

    public static void executeRender(RenderContextImpl r) {
        CustomGui gui = new CustomGui(r.getStateRefs());
        executeRender(r, gui);
    }

    public static void executeRender(RenderContextImpl r, CustomGui gui) {
        RenderDispatcher rd = new RenderDispatcher();
        for (RenderStep step : r.getRootRenderBlock()) {
            step.render(rd, gui);
        }
    }
}
