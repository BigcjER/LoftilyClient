package loftily.module.impl.movement;

import loftily.event.impl.player.motion.MoveEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "SafeWalk",category = ModuleCategory.MOVEMENT)
public class SafeWalk extends Module {
    private final BooleanValue onGround = new BooleanValue("OnGroundSafe",false);
    private final BooleanValue inAir = new BooleanValue("InAirSafe",false);

    @EventHandler
    public void onMove(MoveEvent event) {
        if(event.getEntity() != mc.player)return;
        if((onGround.getValue() && mc.player.onGround) || (inAir.getValue() && !mc.player.onGround)) {
            event.setSafeWalk(true);
        }
    }
}
