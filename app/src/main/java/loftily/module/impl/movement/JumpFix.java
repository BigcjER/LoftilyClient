package loftily.module.impl.movement;

import loftily.event.impl.player.motion.JumpEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "JumpFix",category = ModuleCategory.MOVEMENT)
public class JumpFix extends Module {//Fix Double Jump
    private int jumped = 0;

    @EventHandler(priority = -999)
    public void onJump(JumpEvent event) {
        jumped++;
        if(jumped >= 2) {
            event.setCancelled(true);
            jumped = 0;
        }
    }
}
