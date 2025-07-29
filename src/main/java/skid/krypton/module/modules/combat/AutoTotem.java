package skid.krypton.module.modules.combat;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import skid.krypton.Krypton;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.TickEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.modules.donut.RtpBaseFinder;
import skid.krypton.module.modules.donut.TunnelBaseFinder;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.EncryptedString;

public final class AutoTotem extends Module {

    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 5.0, 1.0, 1.0);
    private int delayCounter;

    public AutoTotem() {
        super(EncryptedString.of("Auto Totem"), EncryptedString.of("Automatically holds totem in your off hand"), -1, Category.COMBAT);
        this.addSettings(this.delay);
    }

    @Override
    public void onEnable() {
        this.delayCounter = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(final TickEvent event) {
        if (this.mc.player == null || this.mc.interactionManager == null) return;

        // Block during Rtp or Tunnel base activity
        if (isBusyWithBaseFinder()) return;

        // Skip if totem is already in offhand
        if (this.mc.player.getInventory().getStack(40).isOf(Items.TOTEM_OF_UNDYING)) {
            this.delayCounter = this.delay.getIntValue();
            return;
        }

        // Delay logic
        if (this.delayCounter > 0) {
            --this.delayCounter;
            return;
        }

        // Find totem in inventory
        final int totemSlot = findItemSlot(Items.TOTEM_OF_UNDYING);
        if (totemSlot == -1) return;

        // Swap into offhand
        this.mc.interactionManager.clickSlot(
            this.mc.player.currentScreenHandler.syncId,
            convertSlotIndex(totemSlot),
            40,
            SlotActionType.SWAP,
            this.mc.player
        );

        this.delayCounter = this.delay.getIntValue();
    }

    private boolean isBusyWithBaseFinder() {
        Module rtp = Krypton.INSTANCE.MODULE_MANAGER.getModuleByClass(RtpBaseFinder.class);
        Module tunnel = Krypton.INSTANCE.MODULE_MANAGER.getModuleByClass(TunnelBaseFinder.class);

        return (rtp != null && rtp.isEnabled() && ((RtpBaseFinder) rtp).isRepairingActive())
            || (tunnel != null && tunnel.isEnabled() && ((TunnelBaseFinder) tunnel).isDigging());
    }

    public int findItemSlot(final Item item) {
        for (int i = 0; i < 36; ++i) {
            if (this.mc.player.getInventory().getStack(i).isOf(item)) {
                return i;
            }
        }
        return -1;
    }

    private static int convertSlotIndex(final int slotIndex) {
        return slotIndex < 9 ? 36 + slotIndex : slotIndex;
    }
}
