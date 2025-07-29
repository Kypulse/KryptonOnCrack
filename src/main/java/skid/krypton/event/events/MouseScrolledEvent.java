package skid.krypton.event.events;

import skid.krypton.event.CancellableEvent;

public class MouseScrolledEvent extends CancellableEvent {
    public double amount;

    public MouseScrolledEvent(final double amount) {
        this.amount = amount;
    }
}