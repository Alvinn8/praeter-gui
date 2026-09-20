package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.slot.SlotPos;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static ca.bkaw.praeter.gui.CommonHooks.useTitle;
import static ca.bkaw.praeter.gui.paper.PaperHooks.getSlotItem;
import static ca.bkaw.praeter.gui.paper.PaperHooks.setSlotItem;

public class ManySlotsExample {
    private static List<Ref<Slot>> LEFT_SLOTS;
    private static List<Ref<Slot>> RIGHT_SLOTS;

    public static final CustomGuiType TYPE = CustomGuiType.builder()
        .height(6)
        .setup(r -> {
            useTitle(r, "A lot of slots.");
            LEFT_SLOTS = new ArrayList<>();
            RIGHT_SLOTS = new ArrayList<>();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 6; y++) {
                    LEFT_SLOTS.add(Slot.slot(r, SlotPos.of(x, y)));
                    RIGHT_SLOTS.add(Slot.slot(r, SlotPos.of(x + 6, y)));
                }
            }
        })
        .build();

    public static void loadContents(CustomGui gui, List<ItemStack> items) {
        for (int i = 0; i < LEFT_SLOTS.size(); i++) {
            setSlotItem(LEFT_SLOTS.get(i).get(gui), items.get(i));
        }
        for (int i = 0; i < RIGHT_SLOTS.size(); i++) {
            setSlotItem(RIGHT_SLOTS.get(i).get(gui), items.get(i + LEFT_SLOTS.size()));
        }
    }

    public static List<ItemStack> saveContents(CustomGui gui) {
        List<ItemStack> items = new ArrayList<>();
        for (Ref<Slot> slotRef : LEFT_SLOTS) {
            items.add(getSlotItem(slotRef, gui));
        }
        for (Ref<Slot> slotRef : RIGHT_SLOTS) {
            items.add(getSlotItem(slotRef, gui));
        }
        return items;
    }
}