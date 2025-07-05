package loftily.module.impl.player.scaffold.towers;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.Scaffold;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class MatrixTowerMove extends Mode<Scaffold> {
    private int state;
    
    public MatrixTowerMove(String name) {
        super(name);
    }
    
    @Override
    public void onToggle() {
        super.onToggle();
        state = 0;
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!getParent().towerStatus) {
            state = 0;
            return;
        }
        
        switch (state) {
            case 0:
                if (!mc.player.onGround) {
                    state = 1;
                }
                break;
            case 1:
                if (mc.player.onGround) {
                    state = 2;
                }
                break;
            
            case 2:
                if (mc.player.onGround) {
                    getParent().fakeJump();
                    mc.player.motionY = 0.42;
                } else if (mc.player.motionY < 0.19) {
                    event.setOnGround(true);
                    mc.player.motionY = 0.42;
                }
                break;
        }
    }
}
