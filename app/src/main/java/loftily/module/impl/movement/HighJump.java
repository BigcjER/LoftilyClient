package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

@ModuleInfo(name = "HighJump",category = ModuleCategory.MOVEMENT)
public class HighJump extends Module {
    private final ModeValue modeValue = new ModeValue("Mode","Vanilla",this,
            new StringMode("Vanilla"),
            new StringMode("Test")
    );
    private final NumberValue motion = new NumberValue("Motion",0.8,0.0,10.0,0.01);
    private final BooleanValue autoToggle = new BooleanValue("AutoToggle",false);
    private final DelayTimer delayTimer = new DelayTimer();
    private int flagCounter = 0;
    private double lastMotionY;
    private boolean boosted = false;

    public void runToggle(){
        if(autoToggle.getValue()){
            this.toggle();
        }
    }

    @Override
    public void onDisable(){
        delayTimer.reset();
        flagCounter = 0;
        boosted = false;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if(event.isPre()) {
            if (modeValue.is("Test")) {
                if (flagCounter <= 1) {
                    if (mc.player.onGround) {
                        mc.player.jump();
                    }else {
                        if(mc.player.fallDistance > 0) {
                            mc.player.motionY = motion.getValue();
                            boosted = true;
                        }
                        if(boosted) {
                            event.setOnGround(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if(modeValue.is("Test")) {
            if (packet instanceof CPacketPlayer.PositionRotation && flagCounter == 2) {
                mc.player.motionY = lastMotionY;
                runToggle();
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if(modeValue.is("Test")) {
            if (packet instanceof SPacketPlayerPosLook) {
                flagCounter++;
                if (flagCounter == 2) {
                    lastMotionY = mc.player.motionY;
                }
            }
        }
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (modeValue.getValue().getName().equals("Vanilla")) {
            event.setCancelled(true);
            mc.player.motionY = motion.getValue();
            runToggle();
        }
    }
}
