package skid.krypton;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import skid.krypton.gui.ClickGUI;
import skid.krypton.manager.ConfigManager;
import skid.krypton.manager.EventManager;
import skid.krypton.module.ModuleManager;

import java.io.File;

public final class Krypton {

    public ConfigManager configManager;
    public ModuleManager MODULE_MANAGER;
    public EventManager EVENT_BUS;
    public static MinecraftClient mc;
    public String version;
    public static Krypton INSTANCE;
    public boolean shouldPreventClose;
    public ClickGUI GUI;
    public Screen screen;
    public long modified;
    public File jar;

    public Krypton() {
        try {
            Krypton.INSTANCE = this;
            this.version = " b1.3";
            this.screen = null;
            this.EVENT_BUS = new EventManager();
            this.MODULE_MANAGER = new ModuleManager();
            this.GUI = new ClickGUI();

            this.jar = new File(Krypton.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            this.modified = this.jar.lastModified();
            this.shouldPreventClose = false;

            // Try to get Minecraft client instance
            Krypton.mc = MinecraftClient.getInstance();

            if (Krypton.mc == null) {
                System.err.println("[Krypton] WARNING: MinecraftClient instance is null! Delaying initialization.");
            } else {
                System.out.println("[Krypton] MinecraftClient instance found successfully.");
                initializeConfig();
            }

        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    // Call this when Minecraft client is guaranteed to be initialized
    public void initializeConfig() {
        this.configManager = new ConfigManager();
        this.configManager.loadProfile();
    }

    // Call this to ensure mc is ready before accessing it
    public static MinecraftClient getMcSafe() {
        if (mc == null) {
            System.err.println("[Krypton] MinecraftClient instance still null!");
        }
        return mc;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public ModuleManager getModuleManager() {
        return this.MODULE_MANAGER;
    }

    public EventManager getEventBus() {
        return this.EVENT_BUS;
    }

    public void resetModifiedDate() {
        this.jar.setLastModified(this.modified);
    }
}
