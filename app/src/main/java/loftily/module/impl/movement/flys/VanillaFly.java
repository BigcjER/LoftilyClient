package loftily.module.impl.movement.flys;

import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.GameSettings;

public class VanillaFly extends Mode<Fly> {

    public VanillaFly() {
        super("Vanilla");
    }
    
    private final NumberValue horizontalSpeed = new NumberValue("HorizontalSpeed", 1, 0, 5, 0.01);
    private final NumberValue verticalSpeed = new NumberValue("VerticalSpeed", 1, 0, 5, 0.01);
    private final NumberValue keepY = new NumberValue("KeepYSpeed", 0.0, 0.0, 0.2, 0.001);
    private final BooleanValue resetMotion = new BooleanValue("ResetMotion", false);

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!MoveUtils.isMoving() && resetMotion.getValue()){
            mc.player.motionX *= 0.0;
            mc.player.motionZ *= 0.0;
            mc.player.motionY *= 0.0;
        }
        MoveUtils.setSpeed(horizontalSpeed.getValue(),true);

        mc.player.motionY *= keepY.getValue();

        if(GameSettings.isKeyDown(mc.gameSettings.keyBindJump)){
            mc.player.motionY = verticalSpeed.getValue();
        }
        if(GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)){
            mc.player.motionY = -verticalSpeed.getValue();
        }
    }
}
