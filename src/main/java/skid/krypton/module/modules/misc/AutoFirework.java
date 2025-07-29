package skid.krypton.module.modules.misc;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import skid.krypton.Krypton;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.PostItemUseEvent;
import skid.krypton.event.events.TickEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BindSetting;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.InventoryUtil;
import skid.krypton.utils.KeyUtils;

public final class AutoFirework extends Module {
    private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), -1, false);
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 0.0, 1.0);
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0).getValue(EncryptedString.of("Delay after using firework before switching back."));
    private boolean isFireworkActive;
    private boolean hasUsedFirework;
    private int useDelayCounter;
    private int previousSelectedSlot;
    private int switchDelayCounter;
    private int cooldownCounter;

    public AutoFirework() {
        super(EncryptedString.of("Auto Firework"), EncryptedString.of("Switches to a firework and uses it when you press a bind."), -1, Category.MISC);
        this.addSettings(this.activateKey, this.delay, this.switchBack, this.switchDelay);
    }

    @Override
    public void onEnable() {
        this.resetState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(final TickEvent event) {
        if (this.mc.currentScreen != null) {
            return;
        }
        if (this.cooldownCounter > 0) {
            --this.cooldownCounter;
            return;
        }
        if (this.mc.player != null && KeyUtils.isKeyPressed(this.activateKey.getValue()) && (Krypton.INSTANCE.MODULE_MANAGER.getModuleByClass(ElytraGlide.class).isEnabled() || this.mc.player.isFallFlying()) && this.mc.player.getInventory().getArmorStack(2).isOf(Items.ELYTRA) && !this.mc.player.getInventory().getMainHandStack().isOf(Items.FIREWORK_ROCKET) && !this.mc.player.getMainHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD) && !(this.mc.player.getMainHandStack().getItem() instanceof ArmorItem)) {
            this.isFireworkActive = true;
        }
        if (this.isFireworkActive) {
            if (this.previousSelectedSlot == -1) {
                this.previousSelectedSlot = this.mc.player.getInventory().selectedSlot;
            }
            if (!InventoryUtil.swap(Items.FIREWORK_ROCKET)) {
                this.resetState();
                return;
            }
            if (this.useDelayCounter < this.delay.getIntValue()) {
                ++this.useDelayCounter;
                return;
            }
            if (!this.hasUsedFirework) {
                this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
                this.hasUsedFirework = true;
            }
            if (this.switchBack.getValue()) {
                this.handleSwitchBack();
            } else {
                this.resetState();
            }
        }
    }

    private void handleSwitchBack() {
        if (this.switchDelayCounter < this.switchDelay.getIntValue()) {
            ++this.switchDelayCounter;
            return;
        }
        InventoryUtil.swap(this.previousSelectedSlot);
        this.resetState();
    }

    private void resetState() {
        this.previousSelectedSlot = -1;
        this.useDelayCounter = 0;
        this.switchDelayCounter = 0;
        this.cooldownCounter = 4;
        this.isFireworkActive = false;
        this.hasUsedFirework = false;
    }

    @EventListener
    public void onPostItemUse(final PostItemUseEvent postItemUseEvent) {
        if (this.mc.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET)) {
            this.hasUsedFirework = true;
        }
        if (this.cooldownCounter > 0) {
            postItemUseEvent.cancel();
        }
    }
}
