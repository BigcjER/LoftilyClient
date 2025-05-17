package loftily.module.impl.render;

import loftily.event.impl.player.MotionEvent;
import loftily.handlers.impl.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.Rotation;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;


@ModuleInfo(name = "Rotations",category = ModuleCategory.Render)
public class Rotations extends Module {

    private final BooleanValue body = new BooleanValue("BodyRotation",true);

    @EventHandler
    public void onMotion(MotionEvent event) {
        Rotation rotation = RotationHandler.clientRotation != null ? RotationHandler.clientRotation : RotationHandler.serverRotation != null ? RotationHandler.serverRotation : new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
        if (body.getValue()) {
            mc.player.renderYawOffset = rotation.yaw;
        }
        mc.player.rotationYawHead = rotation.yaw;
        mc.player.prevRotationYawHead = rotation.yaw;
    }
}
