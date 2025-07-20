package loftily.module.impl.movement.velocitys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.module.impl.movement.Velocity;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

public class NormalVelocity extends Mode<Velocity> {
    public final NumberValue horizontal = new NumberValue("Horizontal", 0.0, -100.0, 100.0, 0.1);
    public final NumberValue vertical = new NumberValue("Vertical", 100.0, -100.0, 100.0, 0.1);
    
    public NormalVelocity() {
        super("Normal");
    }
    
    @EventHandler
    public void onPacket(PacketReceiveEvent event) {
        if (mc.player == null) return;
        if (event.isCancelled())return;
        Packet<?> packet = event.getPacket();
        
        final double horizontal = this.horizontal.getValue();
        final double vertical = this.vertical.getValue();
        
        if (packet instanceof SPacketEntityVelocity) {
            final SPacketEntityVelocity velocity = (SPacketEntityVelocity) packet;

            if(horizontal == 0.0 && vertical == 0.0){
                event.setCancelled(true);
            }

            if (velocity.getEntityID() == mc.player.getEntityId()) {
                velocity.motionX *= (int) (horizontal / 100);
                velocity.motionY *= (int) (vertical / 100);
                velocity.motionZ *= (int) (horizontal / 100);
            }
        }
    }
}
