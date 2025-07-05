package loftily.module.impl.player.scaffold.towers;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.Scaffold;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class NCPTestTower extends Mode<Scaffold> {
    public NCPTestTower(String name) {
        super(name);
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!getParent().towerStatus || event.isPre()) {
            return;
        }
        getParent().fakeJump();

        if (MoveUtils.isMoving()) {
            switch (mc.player.offGroundTicks) {
                case 1:
                case 2:
                    if (mc.player.posY % 1 <= 0.004) {
                        mc.player.motionY = 0.42;
                    }
                    break;
                case 3:
                    break;
                case 4:
                    mc.player.motionY = -0.09800;
                    break;
                case 5:
                    mc.player.motionY /= 0.05;
                    break;
            }
        } else {
            if (mc.player.posY % 1 <= 0.004) {
                mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ);
                mc.player.motionY = 0.42F;
            }
        }
    }
}
