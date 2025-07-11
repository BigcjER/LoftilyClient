package loftily.module.impl.movement.longjumps;

import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.movement.LongJump;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class VanillaLongJump extends Mode<LongJump> {
    private final NumberValue boostSpeed = new NumberValue("Vanilla-BoostSpeed", 3, 0, 5, 0.01);
    private final NumberValue motion = new NumberValue("Vanilla-Motion", 0.42, -1, 1, 0.01);
    
    public VanillaLongJump() {
        super("Vanilla");
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (getParent().getAutoJump())
            mc.player.tryJump();
    }
    
    @EventHandler
    public void onJump(JumpEvent event) {
        event.setCancelled(true);
        MoveUtils.setSpeed(boostSpeed.getValue(), true);
        
        mc.player.motionY = motion.getValue();
        
        if (getParent().getAutoDisable())
            getParent().autoDisable();
    }
}
