package skid.krypton.event.events;

import skid.krypton.event.CancellableEvent;

public class PreItemUseEvent extends CancellableEvent {
    public int cooldown;

    public PreItemUseEvent(final int cooldown) {
        this.cooldown = cooldown;
    }
}