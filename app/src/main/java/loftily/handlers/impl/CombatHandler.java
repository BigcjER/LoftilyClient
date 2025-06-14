package loftily.handlers.impl;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.event.impl.world.WorldLoadEvent;
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
    public void onWorld(WorldLoadEvent event){
        inCombat = false;
        lastTarget = null;
        delayTimer.reset();
    }

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if(mc.player == null) return;
        if(lastTarget == null) {
            inCombat = false;
            delayTimer.reset();
            return;
        }

        inCombat = false;

        if(!delayTimer.hasTimeElapsed(250)){
            inCombat = true;
            return;
        }

        if(lastTarget != null) {
            if (CalculateUtils.getClosetDistance(mc.player, lastTarget) > 6 || !inCombat || !lastTarget.isDead) {
                lastTarget = null;
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof CPacketUseEntity && ((CPacketUseEntity) packet).getAction() == CPacketUseEntity.Action.ATTACK){
            lastTarget = (EntityLivingBase) ((CPacketUseEntity) packet).getEntityFromWorld(mc.world);
            inCombat = true;
            delayTimer.reset();
        }
    }
}
