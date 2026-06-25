package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionalRenderingTest {
    @Test
    public void testRenderIf(@TempDir Path tempDir) throws IOException {
        ResourcePack pack = ResourcePack.loadDirectory(tempDir);
        ResourcePack vanillaAssets = ResourcePack.loadDirectory(Path.of("src/test/resources/vanilla_assets"));

        RenderContextImpl r = new RenderContextImpl(3, pack, vanillaAssets);

        Ref<Boolean> trueRef = r.useState(_ -> true);
        Ref<Boolean> falseRef = r.useState(_ -> false);

        List<String> rendered = new ArrayList<>();
        r.renderIf(trueRef, x -> x, () ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, "true"))
        );
        r.renderIf(falseRef, x -> x, () ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, "false"))
        );

        RenderTestUtils.executeRender(r);

        assertTrue(rendered.contains("true"), "True block should be rendered when condition is true");
        assertFalse(rendered.contains("false"), "False block should not be rendered when condition is false");
    }

    @Test
    public void testRenderIfElse(@TempDir Path tempDir) throws IOException {
        ResourcePack pack = ResourcePack.loadDirectory(tempDir);
        ResourcePack vanillaAssets = ResourcePack.loadDirectory(Path.of("src/test/resources/vanilla_assets"));

        RenderContextImpl r = new RenderContextImpl(3, pack, vanillaAssets);

        Ref<Boolean> trueRef = r.useState(_ -> true);
        Ref<Boolean> falseRef = r.useState(_ -> false);

        List<String> rendered = new ArrayList<>();
        r.renderIf(trueRef, x -> x, () ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, "true1"))
        ).elseRender(() ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, "false1"))
        );
        r.renderIf(falseRef, x -> x, () ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, "true2"))
        ).elseRender(() ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, "false2"))
        );

        RenderTestUtils.executeRender(r);

        assertTrue(rendered.contains("true1"), "True block should be rendered when condition is true");
        assertFalse(rendered.contains("false1"), "False block should not be rendered when condition is true");
        assertFalse(rendered.contains("true2"), "True block should not be rendered when condition is false");
        assertTrue(rendered.contains("false2"), "False block should be rendered when condition is false");
    }

    @Test
    public void testRenderIf_ElseIf_Else(@TempDir Path tempDir) throws IOException {
        ResourcePack pack = ResourcePack.loadDirectory(tempDir);
        ResourcePack vanillaAssets = ResourcePack.loadDirectory(Path.of("src/test/resources/vanilla_assets"));

        RenderContextImpl r = new RenderContextImpl(3, pack, vanillaAssets);

        class State { int value = 1; }
        Ref<State> ref = r.useState(_ -> new State());

        List<Integer> rendered = new ArrayList<>();
        r.renderIf(ref, state -> state.value == 1, () ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, 1))
        ).elseIf(ref, state -> state.value == 2, () ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, 2))
        ).elseRender(() ->
            r.addRenderStep(RenderTestUtils.tracking(rendered, 3))
        );

        CustomGui gui = new CustomGui(r.getStateRefs());

        RenderTestUtils.executeRender(r, gui);
        assertEquals(rendered, List.of(1), "Only the first block should render when ref is 1");

        ref.get(gui).value = 2;
        rendered.clear();
        RenderTestUtils.executeRender(r, gui);
        assertEquals(rendered, List.of(2), "Only the second block should render when ref is 2");

        ref.get(gui).value = 3;
        rendered.clear();
        RenderTestUtils.executeRender(r, gui);
        assertEquals(rendered, List.of(3), "Only the else block should render when ref is 3");
    }
}
