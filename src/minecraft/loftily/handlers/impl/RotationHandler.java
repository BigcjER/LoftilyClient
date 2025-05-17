package loftily.handlers.impl;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.RotationEvent;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.Handler;
import loftily.utils.math.Rotation;
import loftily.utils.player.RotationUtils;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

import static java.lang.Math.round;
import static net.minecraft.util.math.MathHelper.*;

public class RotationHandler extends Handler {

    public static Rotation clientRotation = null;
    public static Rotation serverRotation = null;
    public static int keepRotationTicks = 0;
    public static int backRotationTicks = 0;
    public static int moveFixStatus = 0;

    public static void setClientRotation(Rotation setRotation, Integer keepTicks, Integer backTicks,String moveFix) {
        if (setRotation.pitch > 90 || setRotation.pitch < -90) {
            return;
        }//legit pitch

        clientRotation = setRotation;
        keepRotationTicks = keepTicks;
        backRotationTicks = backTicks;
        if(Objects.equals(moveFix, "Strict")){
            moveFixStatus = 1;
        }else if(Objects.equals(moveFix, "Silent")){
            moveFixStatus = 2;
        }
    }

    @EventHandler
    public void onStrafe(StrafeEvent event) {
        if(clientRotation == null)return;
        if(moveFixStatus > 0){
            event.setYaw(clientRotation.yaw);
            if(moveFixStatus == 2){
                EntityPlayer player = mc.player;

                float diff = (player.rotationYaw - clientRotation.yaw) * (float) (Math.PI / 180);

                float calcForward;
                float calcStrafe;

                float strafe = event.getStrafe();
                float forward = event.getForward();

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

    @EventHandler
    public void onJump(JumpEvent event) {
        if(mc.player == null || mc.world == null)return;
        if(clientRotation == null)return;
        if(moveFixStatus > 0){
            event.setMovementYaw(clientRotation.yaw);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (clientRotation == null) {
            moveFixStatus = 0;
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

    @EventHandler
    public void onRotation(RotationEvent event) {
        if(clientRotation == null) return;
        event.setRotation(clientRotation);
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();

        if (event.isCancelled()) return;

        if (packet instanceof CPacketPlayer) {
            if (((CPacketPlayer) packet).getRotating()) {
                serverRotation = new Rotation(((CPacketPlayer) packet).yaw, ((CPacketPlayer) packet).pitch);
            }
        }
    }

    public static Rotation getRotation() {
        return serverRotation == null ? new Rotation(mc.player.rotationYaw,mc.player.rotationPitch) : serverRotation;
    }
}