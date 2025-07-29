package skid.krypton.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import skid.krypton.font.Fonts;
import skid.krypton.module.modules.client.Krypton;

public final class TextRenderer {
    public static void drawString(final CharSequence charSequence, final DrawContext drawContext, final int n, final int n2, final int n3) {
        if (Krypton.useCustomFont.getValue()) {
            Fonts.FONT.drawString(drawContext.getMatrices(), charSequence, (float) n, (float) n2, n3);
        } else {
            drawLargeString(charSequence, drawContext, n, n2, n3);
        }
    }

    public static int getWidth(final CharSequence charSequence) {
        if (Krypton.useCustomFont.getValue()) {
            return Fonts.FONT.getStringWidth(charSequence);
        }
        return skid.krypton.Krypton.mc.textRenderer.getWidth(charSequence.toString()) * 2;
    }

    public static void drawCenteredString(final CharSequence charSequence, final DrawContext drawContext, final int n, final int n2, final int n3) {
        if (Krypton.useCustomFont.getValue()) {
            Fonts.FONT.drawString(drawContext.getMatrices(), charSequence, (float) (n - Fonts.FONT.getStringWidth(charSequence) / 2), (float) n2, n3);
        } else {
            drawCenteredMinecraftText(charSequence, drawContext, n, n2, n3);
        }
    }

    public static void drawLargeString(final CharSequence charSequence, final DrawContext drawContext, final int n, final int n2, final int n3) {
        final MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.scale(2.0f, 2.0f, 2.0f);
        drawContext.drawText(skid.krypton.Krypton.mc.textRenderer, charSequence.toString(), n / 2, n2 / 2, n3, false);
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.pop();
    }

    public static void drawCenteredMinecraftText(final CharSequence charSequence, final DrawContext drawContext, final int n, final int n2, final int n3) {
        final MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.scale(2.0f, 2.0f, 2.0f);
        drawContext.drawText(skid.krypton.Krypton.mc.textRenderer, (String) charSequence, n / 2 - skid.krypton.Krypton.mc.textRenderer.getWidth((String) charSequence) / 2, n2 / 2, n3, false);
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.pop();
    }
}
