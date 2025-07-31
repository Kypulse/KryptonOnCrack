package skid.krypton.module;

import net.minecraft.client.gui.screen.ChatScreen;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.KeyEvent;
import skid.krypton.module.modules.client.Krypton;
import skid.krypton.module.modules.client.SelfDestruct;
import skid.krypton.module.modules.combat.*;
import skid.krypton.module.modules.donut.*;
import skid.krypton.module.modules.misc.*;
import skid.krypton.module.modules.render.HUD;
import skid.krypton.module.modules.render.ScoreboardHider;
import skid.krypton.module.modules.render.PlayerESP;
import skid.krypton.module.modules.render.StorageESP;
import skid.krypton.module.modules.render.TargetHUD;
import skid.krypton.module.setting.BindSetting;
import skid.krypton.utils.EncryptedString;
import skid.krypton.module.modules.render.Fullbright;//dont judge me alr okay 
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ModuleManager {
    private final List<Module> modules;

    public ModuleManager() {
        this.modules = new ArrayList<>();
        this.a();
        this.d();
    }

    public void a() {
        this.a(new ElytraSwap());
        this.a(new MaceSwap());
        this.a(new Hitbox());
        this.a(new StaticHitboxes());
        this.a(new AutoTotem());
        this.a(new HoverTotem());
        this.a(new AutoInventoryTotem());
        this.a(new AnchorMacro());
        this.a(new AutoCrystal());
        this.a(new DoubleAnchor());
        this.a(new FastPlace());
        this.a(new Freecam());
        this.a(new AutoFirework());
        this.a(new ElytraGlide());
        this.a(new AutoTool());
        this.a(new AutoEat());
        this.a(new AutoMine());
        this.a(new CordSnapper());
        this.a(new KeyPearl());
        this.a(new NameProtect());
        this.a(new AutoTPA());
        this.a(new RtpBaseFinder());
        this.a(new TunnelBaseFinder());
        this.a(new NetheriteFinder());
        this.a(new BoneDropper());
        this.a(new AutoSell());
        this.a(new ShulkerDropper());
        this.a(new AntiTrap());
        this.a(new AuctionSniper());
        this.a(new AutoSpawnerSell());
        this.a(new HUD());
        this.a(new PlayerESP());
        this.a(new StorageESP());
        this.a(new TargetHUD());
        this.a(new Krypton());
        this.a(new SelfDestruct());
        this.a(new ScoreboardHider());
	this.a(new Fullbright());
		
    }

    public List<Module> b() {
        return this.modules.stream().filter(Module::isEnabled).toList();
    }

    public List<Module> c() {
        return this.modules;
    }

    public void d() {
        skid.krypton.Krypton.INSTANCE.getEventBus().register(this);
        for (final Module next : this.modules) {
            next.addSetting(new BindSetting(EncryptedString.of("Keybind"), next.getKeybind(), true).setDescription(EncryptedString.of("Key to enabled the module")));
        }
    }

    public List<Module> a(final Category category) {
        return this.modules.stream().filter(module -> module.getCategory() == category).toList();
    }

    public Module getModuleByClass(final Class<? extends Module> obj) {
        Objects.requireNonNull(obj);
        return this.modules.stream().filter(obj::isInstance).findFirst().orElse(null);
    }

    public void a(final Module module) {
        skid.krypton.Krypton.INSTANCE.getEventBus().register(module);
        this.modules.add(module);
    }

    @EventListener
    public void a(final KeyEvent keyEvent) {
        if (skid.krypton.Krypton.mc.player == null || skid.krypton.Krypton.mc.currentScreen instanceof ChatScreen) {
            return;
        }

        if (!SelfDestruct.isActive) {
            this.modules.forEach(module -> {
                if (module.getKeybind() == keyEvent.key && keyEvent.mode == 1) {
                    module.toggle();
                }
            });
        }
    }
}
