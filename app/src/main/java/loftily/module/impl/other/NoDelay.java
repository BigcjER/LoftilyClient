package loftily.module.impl.other;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "NoDelay",category = ModuleCategory.Other)
public class NoDelay extends Module {
    private final BooleanValue noHitDelay = new BooleanValue("NoClickDelay", false);
    private final BooleanValue noJumpDelay = new BooleanValue("NoJumpDelay", false);
    private final BooleanValue noRightDelay = new BooleanValue("NoRightClickDelay", false);
    private final BooleanValue noBlockHitDelay = new BooleanValue("NoBlockHitDelay",false);
    private final NumberValue rightDelay = new NumberValue("RightClickDelay", 0,0,6).setVisible(noRightDelay::getValue);

    @EventHandler
    public void onMotionEvent(MotionEvent event) {
        if(noHitDelay.getValue()) {
            mc.leftClickCounter = 0;
        }
        if(noJumpDelay.getValue()) {
            mc.player.jumpTicks = 0;
        }
        if(noRightDelay.getValue()) {
            mc.rightClickDelayTimer = rightDelay.getValue().intValue();
        }
        if(noBlockHitDelay.getValue()) {
            mc.playerController.blockHitDelay = 0;
        }
    }
}
