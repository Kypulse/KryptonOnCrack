package skid.krypton.module.modules.render;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.TickEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.utils.EncryptedString;
import skid.krypton.module.modules.client.Krypton;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.ColorUtil;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.Utils;
import skid.krypton.utils.RenderUtils;

public final class ScoreboardHider extends Module {
    private ScoreboardObjective savedObjective;

    public ScoreboardHider() {
        super(EncryptedString.of("Scoreboard Hider"), EncryptedString.of("Hides the sidebar scoreboard"), -1, Category.RENDER);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (mc.world == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective current = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

        if (current != null) {
            if (savedObjective == null) savedObjective = current;
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, null);
        }
    }

    @Override
    public void onDisable() {
        if (mc.world == null || savedObjective == null) return;

        mc.world.getScoreboard().setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, savedObjective);
        savedObjective = null;
    }
}
