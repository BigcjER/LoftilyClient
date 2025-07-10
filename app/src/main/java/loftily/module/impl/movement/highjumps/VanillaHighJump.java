package loftily.module.impl.movement.highjumps;

import loftily.event.impl.player.motion.JumpEvent;
import loftily.module.impl.movement.HighJump;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class VanillaHighJump extends Mode<HighJump> {
    private final NumberValue motion = new NumberValue("Motion", 0.8, 0.0, 10.0, 0.01);
    
    public VanillaHighJump() {
        super("Vanilla");
    }
    
    @EventHandler
    public void onJump(JumpEvent event) {
        event.setCancelled(true);
        mc.player.motionY = motion.getValue();
        //getParent().autoDisable();
    }
}
