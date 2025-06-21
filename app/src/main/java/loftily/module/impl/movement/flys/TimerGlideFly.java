package loftily.module.impl.movement.flys;

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

    private final NumberValue timerSpeed = new NumberValue("TimerSpeed", 20, 10, 100, 0.1);
    private final NumberValue speed = new NumberValue("Speed", 0.02, 0.0, 1.0, 0.01);
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
    private int ticks = 0, jumpCounter = 0, duduTicks = 0;
    private boolean boost = false;
    public TimerGlideFly() {
        super("TimerGlide");
    }

    @Override
    public void onEnable() {
        ticks = 0;
        boost = false;
        lastRotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    @Override
    public void onDisable() {
        ticks = 0;
        boost = false;
        mc.timer.timerSpeed = 1;
        jumpCounter = 0;
        duduTicks = 0;
    }

    @EventHandler
    public void onMotionEvent(MotionEvent event) {
        if (!jumpDamage.getValue()) return;
        if (mc.player.hurtTime <= 0) {
            if (jumpCounter < 4) {
                if (event.isPre()) {
                    event.setOnGround(false);
                }
                mc.player.rotationYaw = lastRotation.yaw;
                mc.player.rotationPitch = lastRotation.pitch;
                mc.player.motionX = mc.player.motionZ = 0.0;
            }
        }
    }

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.player.hurtTime > 0 && !boost) {
            if (mc.player.hurtTime <= 8) {
                mc.player.jump();
            }
            if (mc.player.offGroundTicks >= 3) {
                duduTicks = 20 * timerSpeed.getValue().intValue();
                boost = true;
            }
        }
        if (jumpDamage.getValue()) {
            if (mc.player.hurtTime <= 0) {
                if (jumpCounter < 4) {
                    if (mc.player.onGround) {
                        mc.player.tryJump();
                        jumpCounter++;
                    }
                }
            }
        }

        if (!smartHurt.getValue() || (ticks <= flyTicks.getValue() && boost)) {
            double speedF = duduTicks > 0 ? speed.getValue() : 0.03;
            MoveUtils.setSpeed(speedF, false);
            if (startBoost.getValue()) {
                if (ticks <= boostTicks.getValue()) {
                    MoveUtils.setSpeed(boostSpeed.getValue(), true);
                }
            }
            mc.timer.timerSpeed = timerSpeed.getValue().floatValue();
            if (customMotionY.getValue()) {
                mc.player.motionY = motionSpeed.getValue().floatValue();
            } else {
                mc.player.motionY *= 0.039;
            }
            if (duduTicks > 0) {
                duduTicks--;
            }
            ticks++;

        }

        if (smartHurt.getValue()) {
            if(!smartTicks.getValue()) {
                if(ticks >= flyTicks.getValue()) {
                    getParent().toggle();
                }
            }else {
                if(ticks >= duduTicks){
                    getParent().toggle();
                }
            }
        }
    }
}
