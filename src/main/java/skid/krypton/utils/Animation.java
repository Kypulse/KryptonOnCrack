package skid.krypton.utils;

import skid.krypton.module.modules.client.Krypton;

public final class Animation {
    private double value;
    private final double end;

    public Animation(double end) {
        this.value = end;
        this.end = end;
    }

    public void animate(double speed, double target) {
        if (Krypton.animationMode.isMode(Krypton.AnimationMode.NORMAL)) {
            this.value = MathUtil.approachValue((float) speed, this.value, target);
        } else if (Krypton.animationMode.isMode(Krypton.AnimationMode.POSITIVE)) {
            this.value = MathUtil.smoothStep(speed, this.value, target);
        } else if (Krypton.animationMode.isMode(Krypton.AnimationMode.OFF)) {
            this.value = target;
        }
    }

    public double getAnimation() {
        return this.value;
    }

    public void setAnimation(final double factor) {
        this.value = MathUtil.smoothStep(factor, this.value, this.end);
    }
}
