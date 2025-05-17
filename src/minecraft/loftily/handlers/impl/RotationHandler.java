package loftily.handlers.impl;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.Handler;
import loftily.utils.math.Rotation;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.max;
import static net.minecraft.util.math.MathHelper.atan2;
import static net.minecraft.util.math.MathHelper.sqrt;

public class RotationHandler extends Handler {

    public static Rotation clientRotation = null;
    public static Rotation serverRotation = null;
    public static int keepRotationTicks = 0;
    public static int backRotationTicks = 0;

    public static Rotation toRotation(Vec3d vec, Entity fromEntity) {
        Vec3d eyesPos = fromEntity.getEyes();

        double diffX = vec.xCoord - eyesPos.xCoord;
        double diffY = vec.yCoord - eyesPos.yCoord;
        double diffZ = vec.zCoord - eyesPos.zCoord;

        return new Rotation(
                MathHelper.wrapAngleTo180_float(
                        (float) Math.toDegrees(atan2(diffZ, diffX)) - 90f
                ),
                MathHelper.wrapAngleTo180_float(
                        (float) -Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))
                )
        );
    }


    public static float getAngleDifference(float d1 , float d2) {
        return ((d1 - d2) % 360f + 540f) % 360f - 180f;
    }

    public static Rotation smoothRotation(Rotation from, Rotation to, float yawSpeed , float pitchSpeed){
        float yawDifference = getAngleDifference(to.yaw,from.yaw);
        float pitchDifference = getAngleDifference(to.pitch,from.pitch);

        return new Rotation(
                from.yaw + (yawDifference > yawSpeed ? yawSpeed : Math.max(-yawSpeed, yawDifference)),
                from.pitch + (pitchDifference > pitchSpeed ? pitchSpeed : Math.max(-pitchSpeed, pitchDifference))
        );
    }

    public static void setClientRotation(Rotation setRotation , Integer keepTicks , Integer backTicks){
        if(setRotation.pitch > 90 || setRotation.pitch < -90){
            return;
        }//legit pitch

        clientRotation = setRotation;
        keepRotationTicks = keepTicks;
        backRotationTicks = backTicks;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(clientRotation != null){
            keepRotationTicks--;
            if(keepRotationTicks <= 0){
                if(backRotationTicks > 0){
                    keepRotationTicks = 0;
                    backRotationTicks--;
                    if(backRotationTicks > 0) {
                        clientRotation = new Rotation(
                                clientRotation.yaw - getAngleDifference(clientRotation.yaw,mc.player.rotationYaw) / backRotationTicks,
                                clientRotation.pitch - getAngleDifference(clientRotation.pitch,mc.player.rotationPitch) / backRotationTicks
                        );
                    }
                }else {
                    clientRotation = null;
                }
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event){
        Packet<?> packet = event.getPacket();

        if(event.isCancelled())return;

        if(packet instanceof CPacketPlayer) {

            if (clientRotation != null) {
                ((CPacketPlayer) packet).yaw = clientRotation.yaw;
                ((CPacketPlayer) packet).pitch = clientRotation.pitch;
            }
            if (((CPacketPlayer) packet).getRotating()) {
                serverRotation = new Rotation(((CPacketPlayer) packet).yaw, ((CPacketPlayer) packet).pitch);
            }
        }
    }
}
