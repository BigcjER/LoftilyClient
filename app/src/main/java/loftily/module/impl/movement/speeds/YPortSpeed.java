package loftily.module.impl.movement.speeds;

import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.movement.Speed;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class YPortSpeed extends Mode<Speed> {
    public YPortSpeed() {
        super("YPort");
    }
    
    @EventHandler
    public void onStrafe(StrafeEvent event) {
        MoveUtils.strafe();
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player.isInWater() || mc.player.isInWeb || mc.player.isCollidedHorizontally) return;
        
        if (mc.player.onGround) {
            mc.player.tryJump();
            mc.player.motionY = 0.5;
            return;
        }
        
        if (mc.player.offGroundTicks == 1) {
            mc.player.tryJump();
            mc.player.motionY = -6;
        }
    }
}
