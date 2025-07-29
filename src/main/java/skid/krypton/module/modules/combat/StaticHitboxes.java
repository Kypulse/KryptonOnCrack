package skid.krypton.module.modules.combat;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.TargetPoseEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.utils.EncryptedString;

public final class StaticHitboxes extends Module {
    public StaticHitboxes() {
        super(EncryptedString.of("Static HitBoxes"), EncryptedString.of("Expands a Player's Hitbox"), -1, Category.COMBAT);
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
    public void onTargetPose(final TargetPoseEvent targetPoseEvent) {
        if (this.isEnabled() && targetPoseEvent.entity instanceof PlayerEntity && !((PlayerEntity) targetPoseEvent.entity).isMainPlayer()) {
            targetPoseEvent.cir.setReturnValue(EntityPose.STANDING);
        }
    }
}
