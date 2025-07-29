package skid.krypton.mixin;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import skid.krypton.Krypton;
import skid.krypton.module.modules.misc.NameProtect;

@Mixin({TextVisitFactory.class})
public class TextVisitFactoryMixin {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", ordinal = 0), method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"}, index = 0)
    private static String adjustText(final String s) {
        final NameProtect nameprotect = (NameProtect) Krypton.INSTANCE.MODULE_MANAGER.getModuleByClass(NameProtect.class);
        return nameprotect.isEnabled() && s.contains(Krypton.mc.getSession().getUsername()) ? s.replace(Krypton.mc.getSession().getUsername(), nameprotect.getFakeName()) : s;
    }
}