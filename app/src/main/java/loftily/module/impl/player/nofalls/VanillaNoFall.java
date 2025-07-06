package loftily.module.impl.player.nofalls;

import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.player.NoFall;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.client.CPacketPlayer;

public class VanillaNoFall extends Mode<NoFall> {
    public VanillaNoFall() {
        super("Vanilla");
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (getParent().fallDamage() && getParent().inVoidCheck()) {
            PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
            mc.player.fallDistance = 0;
        }
    }
}
