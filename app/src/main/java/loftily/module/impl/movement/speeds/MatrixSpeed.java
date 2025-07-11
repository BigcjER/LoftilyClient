package loftily.module.impl.movement.speeds;

import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.module.impl.movement.Speed;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;


public class MatrixSpeed extends Mode<Speed> {
    public MatrixSpeed() {
        super("Matrix");
    }
    
    @Override
    public void onDisable() {
        mc.player.jumpMovementFactor = 0.02f;
    }
    
    @EventHandler(priority = -1000)
    public void onJump(JumpEvent event) {
        if (!mc.player.isSprinting()) return;
        
        event.setMovementYaw((float) Math.toDegrees(MoveUtils.getDirection()));
    }
    
    @EventHandler
    public void onStrafe(StrafeEvent event) {
        if (!MoveUtils.isMoving()) return;
        if (mc.player.onGround) {
            mc.player.tryJump();
            MoveUtils.strafe();
        }
        if (MoveUtils.getSpeed() <= 0.2 && !mc.player.isCollidedHorizontally && !mc.player.onGround) {
            MoveUtils.setSpeed(0.2, true);
        }
        if (mc.player.hurtTime <= 0) {
            mc.player.motionY -= 0.0034999;
        }
    }
}
