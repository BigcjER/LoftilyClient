package loftily.handlers.impl;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.RotationEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.Handler;
import loftily.utils.math.Rotation;
import loftily.utils.player.RotationUtils;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

import static net.minecraft.util.math.MathHelper.atan2;
import static net.minecraft.util.math.MathHelper.sqrt;

public class RotationHandler extends Handler {

    public static Rotation clientRotation = null;
    public static Rotation serverRotation = null;
    public static int keepRotationTicks = 0;
    public static int backRotationTicks = 0;

    public static void setClientRotation(Rotation setRotation, Integer keepTicks, Integer backTicks) {
        if (setRotation.pitch > 90 || setRotation.pitch < -90) {
            return;
        }//legit pitch

        clientRotation = setRotation;
        keepRotationTicks = keepTicks;
        backRotationTicks = backTicks;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (clientRotation == null) return;

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