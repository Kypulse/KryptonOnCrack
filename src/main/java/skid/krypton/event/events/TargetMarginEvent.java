package skid.krypton.event.events;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skid.krypton.event.CancellableEvent;

public class TargetMarginEvent extends CancellableEvent {
    public Entity entity;
    public CallbackInfoReturnable<Float> cir;

    public TargetMarginEvent(final Entity entity, final CallbackInfoReturnable<Float> cir) {
        this.entity = entity;
        this.cir = cir;
    }
}