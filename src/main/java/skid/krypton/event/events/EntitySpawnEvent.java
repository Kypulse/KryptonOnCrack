package skid.krypton.event.events;

import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import skid.krypton.event.CancellableEvent;

public class EntitySpawnEvent extends CancellableEvent {
    public EntitySpawnS2CPacket packet;

    public EntitySpawnEvent(final EntitySpawnS2CPacket packet) {
        this.packet = packet;
    }
}