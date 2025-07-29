package skid.krypton.event.events;

import net.minecraft.client.util.Window;
import skid.krypton.event.CancellableEvent;

public class ResolutionChangedEvent extends CancellableEvent {
    public Window window;

    public ResolutionChangedEvent(final Window window) {
        this.window = window;
    }
}