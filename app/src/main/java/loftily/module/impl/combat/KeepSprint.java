package loftily.module.impl.combat;

import loftily.event.impl.player.slowdown.HitSlowDownEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import net.lenni0451.lambdaevents.EventHandler;


@ModuleInfo(name = "KeepSprint", category = ModuleCategory.COMBAT)
public class KeepSprint extends Module {
    private final NumberValue groundMotion = new NumberValue("GroundMotion", 0.6, 0.0, 1.0, 0.01);
    private final NumberValue airMotion = new NumberValue("AirMotion", 0.6, 0.0, 1.0, 0.01);
    private final BooleanValue groundSprint = new BooleanValue("GroundSprint", false);
    private final BooleanValue airSprint = new BooleanValue("AirSprint", false);
    
    @EventHandler
    public void onHitSlowDown(HitSlowDownEvent event) {
        double motion = mc.player.onGround ? groundMotion.getValue() : airMotion.getValue();
        boolean sprint = mc.player.onGround ? groundSprint.getValue() : airSprint.getValue();
        event.setMotionXMultiplier(motion);
        event.setMotionZMultiplier(motion);
        event.setSprint(sprint);
    }
}
