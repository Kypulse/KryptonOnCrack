package skid.krypton.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import skid.krypton.Krypton;
import skid.krypton.module.Module;
import skid.krypton.module.setting.*;

import java.io.File;
import java.nio.file.Files;

public final class ConfigManager {
    private JsonObject jsonObject;
    private final File configFile = new File(Krypton.mc.runDirectory, "krypton/config.json");

    public void loadProfile() {
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                this.jsonObject = new JsonObject(); // Fresh empty config
                return;
            }

            String content = Files.readString(configFile.toPath());
            this.jsonObject = JsonParser.parseString(content).getAsJsonObject();

            for (final Module module : Krypton.INSTANCE.getModuleManager().c()) {
                final JsonElement moduleData = this.jsonObject.get(module.getName().toString());
                if (moduleData != null && moduleData.isJsonObject()) {
                    final JsonObject moduleJson = moduleData.getAsJsonObject();

                    JsonElement enabled = moduleJson.get("enabled");
                    if (enabled != null && enabled.isJsonPrimitive() && enabled.getAsBoolean()) {
                        module.toggle(true);
                    }

                    for (Object obj : module.getSettings()) {
                        Setting setting = (Setting) obj;
                        JsonElement val = moduleJson.get(setting.getName().toString());
                        if (val != null) {
                            setValueFromJson(setting, val, module);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
        }
    }

    private void setValueFromJson(final Setting setting, final JsonElement jsonElement, final Module module) {
        try {
            if (setting instanceof BooleanSetting booleanSetting) {
                if (jsonElement.isJsonPrimitive()) {
                    booleanSetting.setValue(jsonElement.getAsBoolean());
                }
            } else if (setting instanceof ModeSetting enumSetting) {
                if (jsonElement.isJsonPrimitive()) {
                    enumSetting.setModeIndex(jsonElement.getAsInt());
                }
            } else if (setting instanceof NumberSetting numberSetting) {
                if (jsonElement.isJsonPrimitive()) {
                    numberSetting.getValue(jsonElement.getAsDouble());
                }
            } else if (setting instanceof BindSetting bindSetting) {
                if (jsonElement.isJsonPrimitive()) {
                    int key = jsonElement.getAsInt();
                    bindSetting.setValue(key);
                    if (bindSetting.isModuleKey()) {
                        module.setKeybind(key);
                    }
                }
            } else if (setting instanceof StringSetting stringSetting) {
                if (jsonElement.isJsonPrimitive()) {
                    stringSetting.setValue(jsonElement.getAsString());
                }
            } else if (setting instanceof MinMaxSetting minMaxSetting && jsonElement.isJsonObject()) {
                JsonObject obj = jsonElement.getAsJsonObject();
                if (obj.has("min") && obj.has("max")) {
                    minMaxSetting.setCurrentMin(obj.get("min").getAsDouble());
                    minMaxSetting.setCurrentMax(obj.get("max").getAsDouble());
                }
            } else if (setting instanceof ItemSetting itemSetting && jsonElement.isJsonPrimitive()) {
                itemSetting.setItem(Registries.ITEM.get(Identifier.of(jsonElement.getAsString())));
            }
        } catch (Exception ignored) {
        }
    }

    public void shutdown() {
        try {
            this.jsonObject = new JsonObject();

            for (Module module : Krypton.INSTANCE.getModuleManager().c()) {
                JsonObject moduleJson = new JsonObject();
                moduleJson.addProperty("enabled", module.isEnabled());

                for (Setting setting : module.getSettings()) {
                    save(setting, moduleJson, module);
                }

                this.jsonObject.add(module.getName().toString(), moduleJson);
            }

            Files.writeString(configFile.toPath(), this.jsonObject.toString());
        } catch (Exception e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    public void saveProfile() {
        shutdown(); // Save + write to disk
    }

    private void save(final Setting setting, final JsonObject jsonObject, final Module module) {
        try {
            if (setting instanceof BooleanSetting booleanSetting) {
                jsonObject.addProperty(setting.getName().toString(), booleanSetting.getValue());
            } else if (setting instanceof ModeSetting<?> enumSetting) {
                jsonObject.addProperty(setting.getName().toString(), enumSetting.getModeIndex());
            } else if (setting instanceof NumberSetting numberSetting) {
                jsonObject.addProperty(setting.getName().toString(), numberSetting.getValue());
            } else if (setting instanceof BindSetting bindSetting) {
                jsonObject.addProperty(setting.getName().toString(), bindSetting.getValue());
            } else if (setting instanceof StringSetting stringSetting) {
                jsonObject.addProperty(setting.getName().toString(), stringSetting.getValue());
            } else if (setting instanceof MinMaxSetting minMaxSetting) {
                JsonObject obj = new JsonObject();
                obj.addProperty("min", minMaxSetting.getCurrentMin());
                obj.addProperty("max", minMaxSetting.getCurrentMax());
                jsonObject.add(setting.getName().toString(), obj);
            } else if (setting instanceof ItemSetting itemSetting) {
                jsonObject.addProperty(setting.getName().toString(),
                        Registries.ITEM.getId(itemSetting.getItem()).toString());
            }
        } catch (Exception e) {
            System.err.println("Error saving setting " + setting.getName() + ": " + e.getMessage());
        }
    }
}
