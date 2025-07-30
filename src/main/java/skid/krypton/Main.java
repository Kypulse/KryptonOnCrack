package skid.krypton;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public final class Main implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        
        Krypton krypton = new Krypton();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client != null && client.getWindow() != null) {
                client.getWindow().setTitle("Krypton On Crack│https://discord.gg/g3h87TyGs6");
                // Debug log to confirm event firing
                System.out.println("[KryptonOnCrack] Set window title");
            }
        });
    }
}
