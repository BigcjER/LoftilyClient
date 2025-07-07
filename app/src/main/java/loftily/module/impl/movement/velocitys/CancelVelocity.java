package loftily.module.impl.movement.velocitys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.module.impl.movement.Velocity;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class CancelVelocity extends Mode<Velocity> {

    private final BooleanValue cancelXZ = new BooleanValue("CancelHorizontal", true);
    private final BooleanValue cancelY = new BooleanValue("CancelVertical", true);

    public CancelVelocity() {
        super("Cancel");
    }

    @EventHandler
    public void onReceivePacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketEntityVelocity) {
            if (((SPacketEntityVelocity) packet).getEntityID() == mc.player.getEntityId()) {
                event.setCancelled(true);
                if (!cancelXZ.getValue()) {
                    mc.player.motionX = ((SPacketEntityVelocity) packet).getMotionX() / 8000f;
                    mc.player.motionZ = ((SPacketEntityVelocity) packet).getMotionZ() / 8000f;
                }
                if (!cancelY.getValue()) {
                    mc.player.motionY = ((SPacketEntityVelocity) packet).getMotionY() / 8000f;
                }
            }
        }
    }
}
