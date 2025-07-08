package loftily.module.impl.movement.velocitys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.module.impl.movement.Velocity;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class LegitVelocity extends Mode<Velocity> {
    boolean received = false;
    
    public LegitVelocity() {
        super("Legit");
    }
    
    @EventHandler(priority = 2000)
    public void onStrafe(StrafeEvent event) {
        if (mc.player.onGround && received) {
            mc.player.tryJump();
        }
        if (!mc.player.onGround) {
            if (mc.player.hurtTime >= 6) {
                KeyBinding.onTick(mc.gameSettings.keyBindJump.getKeyCode());
            }
            received = false;
        }
    }
    
    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketEntityVelocity) {
            if (((SPacketEntityVelocity) packet).getEntityID() == mc.player.getEntityId()) {
                received = true;
            }
        }
    }
}
