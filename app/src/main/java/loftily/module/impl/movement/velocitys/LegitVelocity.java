package loftily.module.impl.movement.velocitys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.module.impl.movement.Velocity;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class LegitVelocity extends Mode<Velocity> {
    boolean received = false;
    
    public LegitVelocity() {
        super("Legit");
    }
    
    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player.onGround && received) {
            mc.gameSettings.keyBindJump.setPressed(true);
            received = false;
            println("j");
        }
        if (!mc.player.onGround) {
            mc.gameSettings.keyBindJump.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
            received = false;
        }
    }
    
    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled())return;
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketEntityVelocity) {
            if (((SPacketEntityVelocity) packet).getEntityID() == mc.player.getEntityId() && ((SPacketEntityVelocity) packet).getMotionY() > 0) {
                received = true;
            }
        }
    }
}
