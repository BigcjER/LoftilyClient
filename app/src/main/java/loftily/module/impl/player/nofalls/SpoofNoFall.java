package loftily.module.impl.player.nofalls;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.NoFall;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class SpoofNoFall extends Mode<NoFall> {
    public SpoofNoFall() {
        super("Spoof");
    }
    @EventHandler
    public void onMotion(MotionEvent event) {
        if(event.isPost()) {
            return;
        }

        event.setOnGround(true);
    }
}
