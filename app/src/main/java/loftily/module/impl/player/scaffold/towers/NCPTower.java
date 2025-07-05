package loftily.module.impl.player.scaffold.towers;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.Scaffold;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class NCPTower extends Mode<Scaffold> {
    public NCPTower(String name) {
        super(name);
    }
    
    int jumpCounter = 0;
    boolean awaitingGround;
    
    @Override
    public void onToggle() {
        super.onToggle();
        jumpCounter = 0;
        awaitingGround = false;
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!getParent().towerStatus) {
            return;
        }
        
        if (mc.player.posY % 1 <= 0.0014) {
            if (MoveUtils.isMoving()) {
                mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ);
                
                if (mc.player.offGroundTicks < (jumpCounter % 3 == 0 ? 15 : 1)) {
                    mc.player.motionY = 0.42F;
                } else if (!mc.player.onGround && !awaitingGround) {
                    awaitingGround = true;
                    MoveUtils.setSpeed(0, false);
                }
                
                if (awaitingGround && mc.player.onGround) {
                    jumpCounter++;
                    awaitingGround = false;
                }
                
            } else {
                mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ);
                mc.player.motionY = 0.42F;
            }
        }
    }
}
