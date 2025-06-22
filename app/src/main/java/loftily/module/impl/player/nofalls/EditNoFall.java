package loftily.module.impl.player.nofalls;

import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.NoFall;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class EditNoFall extends Mode<NoFall> {
    public EditNoFall() {
        super("Edit");
    }
    @EventHandler
    public void onMotion(MotionEvent event) {
        if(event.isPost()) {
            return;
        }

        if(getParent().fallDamage() && getParent().inVoidCheck()){
            event.setOnGround(true);
            mc.player.fallDistance = 0;
        }
    }
}
