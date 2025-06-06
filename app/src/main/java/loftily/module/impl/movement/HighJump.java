package loftily.module.impl.movement;

import loftily.event.impl.player.motion.JumpEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "HighJump",category = ModuleCategory.MOVEMENT)
public class HighJump extends Module {
    private final ModeValue modeValue = new ModeValue("Mode","Vanilla",this,
            new StringMode("Vanilla"));
    private final NumberValue motion = new NumberValue("Motion",0.8,0.0,10.0,0.01);

    @EventHandler
    public void onJump(JumpEvent event) {
        if (modeValue.getValue().getName().equals("Vanilla")) {
            event.setCancelled(true);
            mc.player.motionY = motion.getValue();
        }
    }
}
