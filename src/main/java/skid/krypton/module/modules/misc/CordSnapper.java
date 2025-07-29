package skid.krypton.module.modules.misc;

import skid.krypton.utils.embed.DiscordWebhook;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.TickEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BindSetting;
import skid.krypton.module.setting.StringSetting;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.KeyUtils;

import java.util.concurrent.CompletableFuture;

public final class CordSnapper extends Module {
    private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), -1, false);
    private final StringSetting webhookUrl = new StringSetting(EncryptedString.of("Webhook"), "");
    private int cooldownCounter;

    public CordSnapper() {
        super(EncryptedString.of("Cord Snapper"), EncryptedString.of("Sends base coordinates to discord webhook"), -1, Category.MISC);
        this.cooldownCounter = 0;
        this.addSettings(this.activateKey, this.webhookUrl);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(final TickEvent event) {
        if (this.mc.player == null) {
            return;
        }
        if (this.cooldownCounter > 0) {
            --this.cooldownCounter;
            return;
        }
        if (KeyUtils.isKeyPressed(this.activateKey.getValue())) {
            DiscordWebhook embedSender = new DiscordWebhook(this.webhookUrl.value);
            embedSender.a("Coordinates: x: " + this.mc.player.getX() + " y: " + this.mc.player.getY() + " z: " + this.mc.player.getZ());
            CompletableFuture.runAsync(() -> {
                try {
                    embedSender.execute();
                } catch (Throwable _t) {
                    _t.printStackTrace(System.err);
                }
            });
            this.cooldownCounter = 40;
        }
    }
}