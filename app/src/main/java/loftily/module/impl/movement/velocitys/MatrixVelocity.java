package loftily.module.impl.movement.velocitys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.module.impl.movement.Velocity;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class MatrixVelocity extends Mode<Velocity> {
    
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
                        MoveUtils.setSpeed(Math.max(
                                MoveUtils.getSpeed(
                                        ((SPacketEntityVelocity) packet).getMotionX() / 8000f,
                                        ((SPacketEntityVelocity) packet).getMotionZ() / 8000f) * 0.1,
                                MoveUtils.getSpeed()), false);
                    } else {
                        MoveUtils.setSpeed(MoveUtils.getSpeed(), true);
                    }
                }
            }
        }
    }
}
