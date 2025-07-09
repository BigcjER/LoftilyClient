package loftily.handlers.impl.player;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.LookEvent;
import loftily.event.impl.player.RotationEvent;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.handlers.Handler;
import loftily.utils.math.Rotation;
import loftily.utils.player.RotationUtils;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;

import static java.lang.Math.round;
import static net.minecraft.util.math.MathHelper.abs;
import static net.minecraft.util.math.MathHelper.ceil;

public class RotationHandler extends Handler {
    
    public static Rotation clientRotation = null;
    public static Rotation serverRotation = null;
    public static int keepRotationTicks = 0;
    public static int backRotationTicks = 0;
    public static MoveFix moveFix = MoveFix.NONE;
    
    public static void setClientRotation(Rotation setRotation, Integer keepTicks, Integer backTicks, String moveFix) {
        MoveFix moveFixEnum = MoveFix.NONE;
        
        switch (moveFix) {
            case "Strict":
                moveFixEnum = MoveFix.STRICT;
                break;
            case "Silent":
                moveFixEnum = MoveFix.SILENT;
                break;
            case "45Angle":
                moveFixEnum = MoveFix.ANGLE_45;
                break;
        }
        
        RotationHandler.setClientRotation(setRotation, keepTicks, backTicks, moveFixEnum);
    }
    
    public static void setClientRotation(Rotation setRotation, Integer keepTicks, Integer backTicks, MoveFix moveFix) {
        //legit pitch
        if (setRotation.pitch > 90 || setRotation.pitch < -90) {
            return;
        }
        
        clientRotation = setRotation;
        keepRotationTicks = keepTicks;
        backRotationTicks = backTicks;
        
        RotationHandler.moveFix = moveFix;
    }
    
    public static Rotation getCurrentRotation() {
        return clientRotation == null ? new Rotation(mc.player.rotationYaw, mc.player.rotationPitch) : clientRotation;
    }
    
    public static Rotation getRotation() {
        return serverRotation == null ? new Rotation(mc.player.rotationYaw, mc.player.rotationPitch) : serverRotation;
    }
    
    @EventHandler(priority = -100)
    public void onStrafe(StrafeEvent event) {
        if (clientRotation == null) return;
        
        if (moveFix != MoveFix.NONE) {
            event.setYaw(clientRotation.yaw);
            if (moveFix.ordinal() >= MoveFix.SILENT.ordinal()) {
                EntityPlayer player = mc.player;
                
                float playerDirection = moveFix == MoveFix.SILENT ? player.rotationYaw : Math.round(player.rotationYaw / 45f) * 45f;
                float diff = (playerDirection - clientRotation.yaw) * (float) (Math.PI / 180);
                
                float calcForward;
                float calcStrafe;
                
                float strafe = event.getStrafe() / 0.98f;
                float forward = event.getForward() / 0.98f;
                
                float modifiedForward = ceil(abs(forward)) * Math.signum(forward);
                float modifiedStrafe = ceil(abs(strafe)) * Math.signum(strafe);
                
                calcForward = round(modifiedForward * MathHelper.cos(diff) + modifiedStrafe * MathHelper.sin(diff));
                calcStrafe = round(modifiedStrafe * MathHelper.cos(diff) - modifiedForward * MathHelper.sin(diff));
                
                float f = (event.getForward() != 0f) ? event.getForward() : event.getStrafe();
                
                calcForward *= abs(f);
                calcStrafe *= abs(f);
                
                event.setForward(calcForward);
                event.setStrafe(calcStrafe);
            }
        }
    }
    
    @EventHandler(priority = -100)
    public void onJump(JumpEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (clientRotation == null) return;
        
        if (moveFix != MoveFix.NONE) {
            event.setMovementYaw(clientRotation.yaw);
        }
    }
    
    @EventHandler(priority = -100)
    public void onUpdate(LivingUpdateEvent event) {
        if (clientRotation == null) {
            moveFix = MoveFix.NONE;
            return;
        }
        
        keepRotationTicks--;
        if (keepRotationTicks > 0) return;
        
        if (backRotationTicks <= 0) {
            clientRotation = null;
            return;
        }
        keepRotationTicks = 0;
        backRotationTicks--;
        
        if (backRotationTicks > 0) {
            clientRotation = new Rotation(
                    clientRotation.yaw - RotationUtils.getAngleDifference(clientRotation.yaw, mc.player.rotationYaw) / backRotationTicks,
                    clientRotation.pitch - RotationUtils.getAngleDifference(clientRotation.pitch, mc.player.rotationPitch) / backRotationTicks
            );
        }
    }
    
    @EventHandler(priority = -100)
    public void onRotation(RotationEvent event) {
        if (clientRotation == null) return;
        
        event.setRotation(clientRotation);
    }
    
    @EventHandler(priority = -100)
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        
        if (event.isCancelled()) return;
        
        if (packet instanceof CPacketPlayer) {
            if (((CPacketPlayer) packet).getRotating()) {
                serverRotation = new Rotation(((CPacketPlayer) packet).yaw, ((CPacketPlayer) packet).pitch);
            }
        }
    }
    
    public enum MoveFix {
        NONE,
        STRICT,
        SILENT,
        ANGLE_45
    }
}