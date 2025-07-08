package loftily.module.impl.render;

import loftily.event.impl.player.RotationEvent;
import loftily.handlers.impl.player.RotationHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;


@ModuleInfo(name = "Rotations", category = ModuleCategory.RENDER)
public class Rotations extends Module {
    public static final BooleanValue vanillaRotation = new BooleanValue("Vanilla", false);
    private final BooleanValue body = new BooleanValue("BodyRotation", true).setVisible(() -> !vanillaRotation.getValue());
    
    @EventHandler
    public void onRotation(RotationEvent event) {
        if (body.getValue() && !vanillaRotation.getValue()) {
            mc.player.renderYawOffset = RotationHandler.getRotation().yaw;
        }
        mc.player.rotationYawHead = RotationHandler.getRotation().yaw;
        mc.player.renderPitchHead = RotationHandler.getRotation().pitch;
    }
}
