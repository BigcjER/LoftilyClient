package loftily.module.impl.player.nofalls;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.NoFall;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class NoGroundNoFall extends Mode<NoFall> {
    public NoGroundNoFall() {
        super("NoGround");
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if(mc.player.onGround){
            event.setOnGround(false);
        }
    }
}
