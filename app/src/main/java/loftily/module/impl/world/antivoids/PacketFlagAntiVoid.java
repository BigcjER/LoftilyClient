package loftily.module.impl.world.antivoids;


import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.world.AntiVoid;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.client.CPacketPlayer;

public class PacketFlagAntiVoid extends Mode<AntiVoid> {
    public PacketFlagAntiVoid() {
        super("PacketFlag");
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (getParent().isSafe()) return;
        
        PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX + 1, mc.player.posY + 1, mc.player.posZ + 1, false), false);
    }
}