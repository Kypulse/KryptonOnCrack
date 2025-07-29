package skid.krypton.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skid.krypton.event.events.PacketSendEvent;
import skid.krypton.event.events.PacketReceiveEvent;
import skid.krypton.manager.EventManager;

@Mixin({ClientConnection.class})
public class ClientConnectionMixin {
    @Inject(method = {"handlePacket"}, at = {@At("HEAD")}, cancellable = true)
    private static void onPacketReceive(final Packet<?> packet, final PacketListener listener, final CallbackInfo ci) {
        final PacketReceiveEvent event = new PacketReceiveEvent(packet);
        EventManager.b(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = {"send(Lnet/minecraft/network/packet/Packet;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void onPacketSend(final Packet<?> packet, final CallbackInfo ci) {
        final PacketSendEvent event = new PacketSendEvent(packet);
        EventManager.b(event);
        if (event.isCancelled()) ci.cancel();
    }
}
