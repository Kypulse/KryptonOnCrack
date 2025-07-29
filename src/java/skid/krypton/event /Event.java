package skid.krypton.event;

public interface Event {
    default boolean isCancelled() {
        return false;
    }
}
