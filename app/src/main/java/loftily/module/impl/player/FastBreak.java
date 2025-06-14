package loftily.module.impl.player;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "FastBreak", category = ModuleCategory.PLAYER)
public class FastBreak extends Module {
    private final NumberValue blockDamage = new NumberValue("BlockDamage", 0.1, 0.0, 1.0, 0.1);
    
    
    @EventHandler
    public void onMotionEvent(MotionEvent event) {
        if (event.isPre()) {
            if (mc.playerController.curBlockDamageMP > blockDamage.getValue()) {
                mc.playerController.curBlockDamageMP = 1;
            }
        }
    }
}
