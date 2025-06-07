package loftily.module.impl.movement.velocitys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class MatrixVelocity extends Mode {

    public MatrixVelocity() {
        super("Matrix");
    }

    @EventHandler
    public void onReceivePacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketEntityVelocity) {
            if (((SPacketEntityVelocity) packet).getEntityID() == mc.player.getEntityId()) {
                event.setCancelled(true);
                if (((SPacketEntityVelocity) packet).getMotionY() / 8000f > 0.22) {
                    mc.player.motionY = ((SPacketEntityVelocity) packet).getMotionY() / 8000f;
                    if (!MoveUtils.isMoving()) {
                        MoveUtils.setSpeed(Math.min(
                                MoveUtils.getSpeed(
                                        ((SPacketEntityVelocity) packet).getMotionX() / 8000f,
                                        ((SPacketEntityVelocity) packet).getMotionZ() / 8000f),
                                0.4), false);
                    } else {
                        MoveUtils.setSpeed(MoveUtils.getSpeed((float) mc.player.motionX, (float) mc.player.motionZ), false);
                    }
                }
            }
        }
    }
}
