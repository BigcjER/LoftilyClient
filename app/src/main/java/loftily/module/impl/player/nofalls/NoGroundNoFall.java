package loftily.module.impl.player.nofalls;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.impl.player.NoFall;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

public class NoGroundNoFall extends Mode<NoFall> {
    public NoGroundNoFall() {
        super("NoGround");
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof CPacketPlayer){
            ((CPacketPlayer) packet).onGround = true;
        }
    }
}
