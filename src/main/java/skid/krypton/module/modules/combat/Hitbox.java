package skid.krypton.module.modules.combat;

import net.minecraft.entity.player.PlayerEntity;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.TargetMarginEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.EncryptedString;

public final class Hitbox extends Module {
    private final NumberSetting expand = new NumberSetting(EncryptedString.of("Expand"), 0.0, 2.0, 0.5, 0.05);
    private final BooleanSetting enableRender = new BooleanSetting("Enable Render", true);

    public Hitbox() {
        super(EncryptedString.of("HitBox"), EncryptedString.of("Expands a player's hitbox."), -1, Category.COMBAT);
        this.addSettings(this.enableRender, this.expand);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTargetMargin(final TargetMarginEvent targetMarginEvent) {
        if (targetMarginEvent.entity instanceof PlayerEntity) {
            targetMarginEvent.cir.setReturnValue((float) this.expand.getValue());
        }
    }

    public double getHitboxExpansion() {
        if (!this.enableRender.getValue()) {
            return 0.0;
        }
        return this.expand.getValue();
    }
}