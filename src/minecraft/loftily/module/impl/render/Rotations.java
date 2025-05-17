package loftily.module.impl.render;

import loftily.event.impl.player.RotationEvent;
import loftily.handlers.impl.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.player.RotationUtils;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;


@ModuleInfo(name = "Rotations", category = ModuleCategory.Render)
public class Rotations extends Module {
    
    private final BooleanValue body = new BooleanValue("BodyRotation", true);
    
    @EventHandler
    public void onRotation(RotationEvent event) {
        if (body.getValue()) {
            mc.player.renderYawOffset = RotationUtils.getRotation().yaw;
        }
        mc.player.rotationYawHead = RotationUtils.getRotation().yaw;
        mc.player.renderPitchHead = RotationUtils.getRotation().pitch;
    }
}
