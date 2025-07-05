package loftily.module.impl.player.scaffold.towers;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.Scaffold;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class NCPTowerMove extends Mode<Scaffold> {
    public NCPTowerMove(String name) {
        super(name);
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!getParent().towerStatus) {
            return;
        }
        
        getParent().fakeJump();
        if (mc.player.posY % 1 <= 0.0014) {
            if (MoveUtils.isMoving()) {
                mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ);
                
                double speedLimit = 0.008;
                if (MoveUtils.getSpeed() <= speedLimit) {
                    mc.player.motionY = 0.38F;
                } else {
                    MoveUtils.setSpeed(speedLimit, false);
                }
                
                mc.player.setSprinting(false);
            } else {
                mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ);
                mc.player.motionY = 0.42F;
            }
        }
    }
}
