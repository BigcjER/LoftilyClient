package loftily.module.impl.player.scaffold.towers;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.Scaffold;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class VanillaTower extends Mode<Scaffold> {
    public VanillaTower(String name) {
        super(name);
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!getParent().towerStatus) return;
        getParent().fakeJump();
        mc.player.motionY = 0.42;
    }
}
