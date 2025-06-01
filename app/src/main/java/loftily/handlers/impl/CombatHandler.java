package loftily.handlers.impl;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.handlers.Handler;
import loftily.utils.math.CalculateUtils;
import loftily.utils.timer.DelayTimer;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;

public class CombatHandler extends Handler {
    public static boolean inCombat = false;
    public static EntityLivingBase lastTarget = null;
    public static DelayTimer delayTimer = new DelayTimer();

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if(lastTarget == null) {
            delayTimer.reset();
            return;
        }

        if(delayTimer.hasTimeElapsed(250)){
            inCombat = false;
            delayTimer.reset();
        }

        if(CalculateUtils.getClosetDistance(mc.player, lastTarget) > 7) {
            lastTarget = null;
            inCombat = false;
        }
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof CPacketUseEntity && ((CPacketUseEntity) packet).getAction() == CPacketUseEntity.Action.ATTACK){
            lastTarget = (EntityLivingBase) ((CPacketUseEntity) packet).getEntityFromWorld(mc.world);
            inCombat = true;
        }
    }
}
