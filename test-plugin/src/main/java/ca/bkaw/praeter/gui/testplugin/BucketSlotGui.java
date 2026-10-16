package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.slot.SlotPos;
import org.bukkit.Material;

import static ca.bkaw.praeter.gui.CommonHooks.*;
import static ca.bkaw.praeter.gui.paper.PaperHooks.*;

public class BucketSlotGui {
    private static Ref<BucketSlotGui> STATE;
    private static Ref<Button> TYPE_BUTTON;
    private static Ref<Button> LOCK_BUTTON;
    private static Ref<Slot> SLOT;

    enum BucketType {
        WATER(Material.WATER_BUCKET),
        LAVA(Material.LAVA_BUCKET);

        private final Material material;

        BucketType(Material material) {
            this.material = material;
        }
    }

    private BucketType type;
    private boolean locked;

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(1)
        .setup(r -> {
            useTitle(r, "Bucket Slot GUI");
            STATE = useState(r, BucketSlotGui::new);
            TYPE_BUTTON = Button.button(r, "Change type", SlotPos.of(0, 0), 4, 1);
            LOCK_BUTTON = Button.button(r, "Toggle lock", SlotPos.of(5, 0), 4, 1);
            SLOT = Slot.slot(r, SlotPos.of(4, 0), slotBehavior(
                (gui, item) -> {
                    Material material = STATE.get(gui).type.material;
                    return item.getType() == material;
                },
                (gui, player) -> !STATE.get(gui).locked
            ));
        })
        .build();

    public BucketSlotGui(CustomGui gui) {
        this.type = BucketType.WATER;
        this.locked = false;
        onClick(gui, TYPE_BUTTON.get(gui), (ctx) -> {
            this.toggleType();
            gui.update();
        });
        onClick(gui, LOCK_BUTTON.get(gui), (ctx) -> {
            this.toggleLock();
            gui.update();
        });
    }

    public void toggleType() {
        if (this.type == BucketType.WATER) {
            this.type = BucketType.LAVA;
        } else {
            this.type = BucketType.WATER;
        }
    }

    public void toggleLock() {
        this.locked = !this.locked;
    }
}
