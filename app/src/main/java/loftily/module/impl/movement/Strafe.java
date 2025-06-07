package loftily.module.impl.movement;

import loftily.event.impl.player.motion.StrafeEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import net.lenni0451.lambdaevents.EventHandler;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static net.minecraft.util.math.MathHelper.sqrt;

@ModuleInfo(name = "Strafe",category = ModuleCategory.MOVEMENT)
public class Strafe extends Module {
    private final BooleanValue onGround = new BooleanValue("OnGround",false);
    private final BooleanValue inAir = new BooleanValue("InAir",false);
    private final NumberValue delay = new NumberValue("Delay",1,1,12);
    private final BooleanValue onlyHurt = new BooleanValue("OnlyHurt",false);

    @EventHandler
    public void onStrafe(StrafeEvent event) {
        if(onlyHurt.getValue() && mc.player.hurtTime <= 0)return;

        if(mc.player.ticksExisted % delay.getValue() == 0) {
            if ((onGround.getValue() && mc.player.onGround) || (inAir.getValue() && !mc.player.onGround)) {
                double speed = sqrt((mc.player.motionX * mc.player.motionX) + (mc.player.motionZ * mc.player.motionZ));
                MoveUtils.setSpeed(speed,true);
            }
        }
    }
}
