package skid.krypton.utils;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import skid.krypton.Krypton;
import skid.krypton.mixin.ClientPlayerInteractionManagerAccessor;

import java.util.function.Predicate;

public final class InventoryUtil {
    public static void swap(final int selectedSlot) {
        if (selectedSlot < 0 || selectedSlot > 8) {
            return;
        }
        Krypton.mc.player.getInventory().selectedSlot = selectedSlot;
        ((ClientPlayerInteractionManagerAccessor) Krypton.mc.interactionManager).syncSlot();
    }

    public static boolean swapStack(final Predicate<ItemStack> predicate) {
        final PlayerInventory getInventory = Krypton.mc.player.getInventory();
        for (int i = 0; i < 9; ++i) {
            if (predicate.test(getInventory.getStack(i))) {
                getInventory.selectedSlot = i;
                return true;
            }
        }
        return false;
    }

    public static boolean swapItem(final Predicate<Item> predicate) {
        final PlayerInventory getInventory = Krypton.mc.player.getInventory();
        for (int i = 0; i < 9; ++i) {
            if (predicate.test(getInventory.getStack(i).getItem())) {
                getInventory.selectedSlot = i;
                return true;
            }
        }
        return false;
    }

    public static boolean swap(Item item) {
        return InventoryUtil.swapItem((Item item2) -> item2 == item);
    }

    public static int getSlot(final Item obj) {
        final ScreenHandler currentScreenHandler = Krypton.mc.player.currentScreenHandler;
        if (Krypton.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            int n = 0;
            for (int i = 0; i < ((GenericContainerScreenHandler) Krypton.mc.player.currentScreenHandler).getRows() * 9; ++i) {
                if (currentScreenHandler.getSlot(i).getStack().getItem().equals(obj)) {
                    ++n;
                }
            }
            return n;
        }
        return 0;
    }
}