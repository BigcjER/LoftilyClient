package loftily.module.impl.combat.criticals;

import loftily.event.impl.player.AttackEvent;
import loftily.module.impl.combat.Criticals;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;

public class TPHopCriticals extends Mode<Criticals> {
    public TPHopCriticals() {
        super("TPHop");
    }
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.02, mc.player.posZ, false));
            PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.01, mc.player.posZ, false));
            mc.player.setPosition(mc.player.posX, mc.player.posY + 0.01, mc.player.posZ);
        }
    }
}
