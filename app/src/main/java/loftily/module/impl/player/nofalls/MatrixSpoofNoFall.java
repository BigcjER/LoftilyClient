package loftily.module.impl.player.nofalls;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.module.impl.player.NoFall;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

public class MatrixSpoofNoFall extends Mode<NoFall> {
    private final BooleanValue legitTimer = new BooleanValue("LegitTimer", false);
    private boolean timered = false;
    
    public MatrixSpoofNoFall() {
        super("MatrixSpoof");
    }
    
    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
        timered = false;
    }
    
    @EventHandler
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (getParent().fallDamage() && getParent().inVoidCheck()) {
            if (packet instanceof CPacketPlayer) {
                if (((CPacketPlayer) packet).getMoving()) {
                    event.setCancelled(true);
                    PacketUtils.sendPacket(new CPacketPlayer.Position(
                            ((CPacketPlayer) packet).x, ((CPacketPlayer) packet).y, ((CPacketPlayer) packet).z, true
                    ), false);
                    PacketUtils.sendPacket(new CPacketPlayer.Position(
                            ((CPacketPlayer) packet).x, ((CPacketPlayer) packet).y, ((CPacketPlayer) packet).z, false
                    ), false);
                    mc.player.fallDistance = 0;
                    if (legitTimer.getValue()) {
                        timered = true;
                        mc.timer.timerSpeed = 0.2F;
                    }
                }
            }
        } else if (timered) {
            mc.timer.timerSpeed = 1F;
            timered = false;
        }
    }
}
