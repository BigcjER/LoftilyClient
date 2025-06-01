package loftily.handlers.impl;

import loftily.event.impl.client.MoveInputEvent;
import loftily.event.impl.render.Render3DEvent;
import loftily.handlers.Handler;
import loftily.utils.timer.DelayTimer;
import net.lenni0451.lambdaevents.EventHandler;

public class MoveHandler extends Handler {
    //Sneak
    public static boolean sneak = false;
    public static int sneakTime = 0;
    public static DelayTimer sneakTimer = new DelayTimer();
    private static boolean startSneak = false;

    public static void setSneak(boolean canSneak, int time) {
        sneak = canSneak;
        sneakTime = time;
        startSneak = true;
        sneakTimer.reset();
    }

    @EventHandler(priority = -999999)
    public void onMoveInput(MoveInputEvent event) {
        if (sneak) {
            event.setSneak(true);
            if (!startSneak) {
                sneak = false;
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (sneakTimer.hasTimeElapsed(sneakTime)) {
            startSneak = false;
            sneakTimer.reset();
        }
        if (startSneak) {
            sneak = true;
        } else {
            sneakTimer.reset();
        }
    }
}
