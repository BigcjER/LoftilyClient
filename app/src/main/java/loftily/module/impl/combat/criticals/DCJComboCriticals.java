package loftily.module.impl.combat.criticals;

import loftily.event.impl.player.AttackEvent;
import loftily.module.impl.combat.Criticals;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;

public class DCJComboCriticals extends Mode<Criticals> {
    public DCJComboCriticals() {
        super("DCJCombo");
    }
    
    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            if (event.getTarget().hurtResistantTime <= 10) {
                PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.5, mc.player.posZ, true));
                PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            }
        }
    }
}
