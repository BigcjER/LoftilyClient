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
        Packet<?> packet = event.getPacket();
        
        final double horizontal = this.horizontal.getValue();
        final double vertical = this.vertical.getValue();
        
        if (packet instanceof SPacketEntityVelocity) {
            final SPacketEntityVelocity velocity = (SPacketEntityVelocity) packet;
            
            if (velocity.getEntityID() != mc.player.getEntityId()) return;
            
            event.setCancelled(true);
            
            if (horizontal == 0 && vertical == 0) {
                return;
            }
            
            if (horizontal != 0) {
                mc.player.motionX = ((SPacketEntityVelocity) packet).getMotionX() / 8000f;
                mc.player.motionZ = ((SPacketEntityVelocity) packet).getMotionZ() / 8000f;
            }
            
            if (vertical != 0) {
                mc.player.motionY = ((SPacketEntityVelocity) packet).getMotionY() / 8000f;
            }
        }
    }
}
