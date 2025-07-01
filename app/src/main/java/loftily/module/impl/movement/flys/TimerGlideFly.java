package loftily.module.impl.movement.flys;

import loftily.event.impl.client.MoveInputEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.math.Rotation;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class TimerGlideFly extends Mode<Fly> {
    
    private final NumberValue timerSpeed = new NumberValue("TimerSpeed", 20, 1, 100, 0.1);
    private final NumberValue speed = new NumberValue("Speed", 0.02, 0.0, 1.0, 0.01);
    private final BooleanValue noSpeed = new BooleanValue("NoSpeed", false);
    private final BooleanValue customMotionY = new BooleanValue("CustomMotionY", false);
    private final NumberValue motionSpeed = new NumberValue("MotionSpeed", -0.01, -0.2, 0.2, 0.01).setVisible(customMotionY::getValue);
    private final BooleanValue smartHurt = new BooleanValue("SmartHurt", false);
    private final BooleanValue smartTicks = new BooleanValue("SmartTicks", false);
    private final NumberValue flyTicks = new NumberValue("FlyTicks", 900, 0, 1600).setVisible(smartHurt::getValue);
    private final BooleanValue startBoost = new BooleanValue("StartBoost", false);
    private final NumberValue boostTicks = new NumberValue("BoostTicks", 7, 1, 300).setVisible(startBoost::getValue);
    private final NumberValue boostSpeed = new NumberValue("BoostSpeed", 0.6, 0.2, 1.0, 0.01).setVisible(startBoost::getValue);
    private final BooleanValue jumpDamage = new BooleanValue("JumpDamage", false);
    private Rotation lastRotation = new Rotation(0, 0);
    private int elapsedTicks, jumpCounter, boostDurationTicks;
    private boolean boosting;
    
    public TimerGlideFly() {
        super("TimerGlide");
    }
    
    @Override
    public void onEnable() {
        elapsedTicks = 0;
        boosting = false;
        lastRotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
    }
    
    @Override
    public void onDisable() {
        elapsedTicks = jumpCounter = boostDurationTicks = 0;
        boosting = false;
        mc.timer.timerSpeed = 1;
    }
    
    @EventHandler
    public void onMotionEvent(MotionEvent event) {
        if (!jumpDamage.getValue()) return;
        
        if (mc.player.hurtTime > 0) return;
        
        if (jumpCounter < 4) {
            if (event.isPre()) {
                event.setOnGround(false);
            }
            mc.player.rotationYaw = lastRotation.yaw;
            mc.player.rotationPitch = lastRotation.pitch;
        }
    }
    
    @EventHandler
    public void onMoveInput(MoveInputEvent event) {
        if (jumpDamage.getValue() && !boosting) {
            event.setSneak(false);
            event.setJump(false);
            event.setStrafe(0);
            event.setForward(0);
        }
    }
    
    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.player.hurtTime > 0 && !boosting) {
            if (mc.player.hurtTime <= 8) {
                mc.player.jump();
            }
            
            if (mc.player.offGroundTicks >= 3) {
                boostDurationTicks = 20 * timerSpeed.getValue().intValue();
                boosting = true;
            }
        }
        
        if (jumpDamage.getValue()) {
            if (mc.player.hurtTime <= 0 && jumpCounter < 4) {
                if (mc.player.onGround) {
                    mc.player.tryJump();
                    jumpCounter++;
                }
            }
        }
        
        if (!smartHurt.getValue() || (elapsedTicks <= flyTicks.getValue() && boosting)) {
            double speedF = boostDurationTicks > 0 ? speed.getValue() : 0.03;
            if(!noSpeed.getValue()) {
                MoveUtils.setSpeed(speedF, false);
            }
            
            if (startBoost.getValue()) {
                if (elapsedTicks <= boostTicks.getValue()) {
                    MoveUtils.setSpeed(boostSpeed.getValue(), true);
                }
            }
            
            mc.timer.timerSpeed = timerSpeed.getValue().floatValue();
            
            if (customMotionY.getValue()) {
                mc.player.motionY = motionSpeed.getValue().floatValue();
            } else {
                mc.player.motionY *= 0.039;
            }
            
            if (boostDurationTicks > 0) {
                boostDurationTicks--;
            }
            
            elapsedTicks++;
        }
        
        if (smartHurt.getValue() && boosting) {
            if (!smartTicks.getValue()) {
                if (elapsedTicks >= flyTicks.getValue()) {
                    getParent().toggle();
                }
            } else {
                if (elapsedTicks >= boostDurationTicks) {
                    getParent().toggle();
                }
            }
        }
    }
}