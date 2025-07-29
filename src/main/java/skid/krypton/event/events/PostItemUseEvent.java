package skid.krypton.event.events;

import skid.krypton.event.CancellableEvent;

public class PostItemUseEvent extends CancellableEvent {
    public int cooldown;

    public PostItemUseEvent(final int cooldown) {
        this.cooldown = cooldown;
    }
}