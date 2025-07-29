package skid.krypton.module.modules.misc;

import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.Setting;
import skid.krypton.module.setting.StringSetting;
import skid.krypton.utils.EncryptedString;

public class NameProtect extends Module {
    private final StringSetting fakeName = new StringSetting("Fake Name", "Player");

    public NameProtect() {
        super(EncryptedString.of("Name Protect"), EncryptedString.of("Replaces your name with given one."), -1, Category.MISC);
        this.addSettings(this.fakeName);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public String getFakeName() {
        return this.fakeName.getValue();
    }
}
