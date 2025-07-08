package loftily.module.impl.movement.flys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class MatrixBoostFly extends Mode<Fly> {
    private final NumberValue height = new NumberValue("Height", 1, 0.42, 7, 0.1);
    private int jumpCounter = 0;
    private boolean receivedFlag, canBoost;
    private double lastMotionX, lastMotionY, lastMotionZ;
    
    public MatrixBoostFly() {
        super("MatrixBoost");
    }
    
    @Override
    public void onEnable() {
        jumpCounter = 0;
        canBoost = false;
        receivedFlag = false;
    }
    
    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (jumpCounter >= 4) {
            if (canBoost) {
                MoveUtils.setSpeed(1.97, false);
                mc.player.motionY = height.getValue();
            }
            
            if (mc.player.hurtTime >= 1 && mc.player.hurtTime <= 8) {
                if (mc.player.onGround) {
                    mc.player.tryJump();
                } else {
                    if (mc.player.motionY < 0.2) {
                        canBoost = true;
                    }
                }
            }
        }
        if (jumpCounter < 4) {
            if (mc.player.onGround) {
                mc.player.tryJump();
                jumpCounter += 1;
            }
        }
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (jumpCounter < 4) {
            event.setOnGround(false);
        }
    }
    
    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketPlayer.PositionRotation && receivedFlag) {
            receivedFlag = false;
            mc.player.motionX = lastMotionX;
            mc.player.motionY = lastMotionY;
            mc.player.motionZ = lastMotionZ;
            this.getParent().toggle();
        }
    }
    
    @EventHandler
    public void onReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (canBoost) {
            if (packet instanceof SPacketPlayerPosLook) {
                receivedFlag = true;
                lastMotionX = mc.player.motionX;
                lastMotionY = mc.player.motionY;
                lastMotionZ = mc.player.motionZ;
                canBoost = false;
            }
        }
    }
}
