package loftily.module.impl.combat.criticals;


import loftily.event.impl.packet.PacketSendEvent;
import loftily.module.impl.combat.Criticals;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.client.CPacketPlayer;

public class NoGroundCriticals extends Mode<Criticals> {
    public NoGroundCriticals() {
        super("NoGround");
    }
    
    @Override
    public void onEnable() {
        mc.player.tryJump();
        super.onEnable();
    }
    
    @EventHandler
    public void onPacket(PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer CPacketPlayer = (CPacketPlayer) event.getPacket();
            CPacketPlayer.onGround = false;
        }
    }
}
