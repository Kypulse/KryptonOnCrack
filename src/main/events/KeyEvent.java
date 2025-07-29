package skid.krypton.event.events;

import skid.krypton.event.CancellableEvent;

public class KeyEvent extends CancellableEvent {
    public int key;
    public int mode;
    public long window;

    public KeyEvent(final int key, final long window, final int mode) {
        this.key = key;
        this.window = window;
        this.mode = mode;
    }
}