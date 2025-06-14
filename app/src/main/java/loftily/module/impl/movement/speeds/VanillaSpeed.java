package loftily.module.impl.movement.speeds;

import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.movement.Speed;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class VanillaSpeed extends Mode<Speed> {
    public VanillaSpeed() {
        super("Vanilla");
    }
    
    public NumberValue vanillaSpeed = new NumberValue("Speed", 0.4, 0.1, 2, 0.1);
    public BooleanValue vanillaJump = new BooleanValue("Jump", true);
    public BooleanValue vanillaFastStop = new BooleanValue("FastStop", true);
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!MoveUtils.isMoving() && vanillaFastStop.getValue()) {
            MoveUtils.stop();
            return;
        }
        
        if (mc.player.onGround && vanillaJump.getValue() && MoveUtils.isMoving()) {
            mc.player.tryJump();
        }
        
        MoveUtils.setSpeed(vanillaSpeed.getValue(), true);
    }
}
