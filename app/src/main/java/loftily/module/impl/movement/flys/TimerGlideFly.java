package loftily.module.impl.movement.flys;

import loftily.event.impl.client.MoveInputEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.Rotation;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.client.CPacketPlayer;

public class TimerGlideFly extends Mode<Fly> {
    
    private final NumberValue timerSpeed = new NumberValue("TimerSpeed", 20, 1, 100, 0.1);
    private final NumberValue speed = new NumberValue("Speed", 0.02, 0.0, 1.0, 0.01);
    private final BooleanValue noSpeed = new BooleanValue("NoSpeed", false);
    private final BooleanValue smartHurt = new BooleanValue("SmartHurt", false);
    private final BooleanValue smartTicks = new BooleanValue("SmartTicks", false);
    private final NumberValue flyTicks = new NumberValue("FlyTicks", 900, 0, 1600).setVisible(smartHurt::getValue);
    private final BooleanValue jumpDamage = new BooleanValue("JumpDamage", false);
    private final BooleanValue disablePacket = new BooleanValue("PacketOnDisable", false);
    private final ModeValue customMotionY = new ModeValue("CustomMotionY", "None", this
            , new StringMode("None"), new StringMode("Stable"), new StringMode("Multiply"));
    private final NumberValue motionSpeed = new NumberValue("Motion", -0.01, -0.3, 0.3, 0.01).setVisible(() -> !customMotionY.is("None"));
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
        if(disablePacket.getValue()) {
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ,
                    mc.player.rotationYaw,mc.player.rotationPitch,false));
            PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ,
                    mc.player.rotationYaw,mc.player.rotationPitch,false));
        }
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
            if (!noSpeed.getValue()) {
                MoveUtils.setSpeed(speedF, false);
            }
            
            mc.timer.timerSpeed = timerSpeed.getValue().floatValue();

            switch (customMotionY.getValueByName()) {
                case "None":
                    mc.player.motionY *= 0.042;
                    break;
                case "Stable":
                    mc.player.motionY = motionSpeed.getValue().floatValue();
                    break;
                case "Multiply":
                    mc.player.motionY *= motionSpeed.getValue().floatValue();
                    break;
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
                if (boostDurationTicks <= 0) {
                    getParent().toggle();
                }
            }
        }
    }
}