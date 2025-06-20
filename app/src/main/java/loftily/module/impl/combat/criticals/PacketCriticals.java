package loftily.module.impl.combat.criticals;


import loftily.event.impl.player.AttackEvent;
import loftily.module.impl.combat.Criticals;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.client.CPacketPlayer;

public class PacketCriticals extends Mode<Criticals> {
    public PacketCriticals() {
        super("Packet");
    }
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        sendCriticalPacket(0, 0.0625, 0, true);
        sendCriticalPacket(0, 0, 0, false);
        sendCriticalPacket(0, 1.1E-5, 0, false);
        sendCriticalPacket(0, 0, 0, false);
    }
    
    private void sendCriticalPacket(double xOffset, double yOffset, double zOffset, boolean ground) {
        double x = mc.player.posX + xOffset;
        double y = mc.player.posY + yOffset;
        double z = mc.player.posZ + zOffset;
        PacketUtils.sendPacket(new CPacketPlayer.Position(x, y, z, ground));
    }
}
