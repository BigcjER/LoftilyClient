package loftily.handlers.impl.client;

import loftily.event.impl.client.ClientTickEvent;
import loftily.event.impl.client.ConnectServerEvent;
import loftily.event.impl.player.AttackEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.Handler;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.entity.EntityLivingBase;

public class PlayerStateHandler extends Handler {
    private EntityLivingBase target;
    
    @EventHandler(priority = 1000)
    public void onTick(ClientTickEvent event) {
        mc.player.movementYaw = mc.player.rotationYaw;
    }
    
    @EventHandler(priority = 1000)
    public void onUpdate(UpdateEvent event) {
        mc.player.prevRenderPitchHead = mc.player.renderPitchHead;
        mc.player.renderPitchHead = mc.player.rotationPitch;
        
        if (this.target != null && !mc.world.loadedEntityList.contains(this.target)) {
            mc.player.kills++;
            target = null;
        }
    }
    
    @EventHandler(priority = 1000)
    public void onConnectServer(ConnectServerEvent event) {
        mc.prevServerData = event.getServerData();
        mc.getSession().setSessionStartTime(System.currentTimeMillis());
        mc.player.kills = 0;
    }
    
    @EventHandler(priority = 1000)
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            this.target = (EntityLivingBase) event.getTarget();
        }
    }
    
    @EventHandler(priority = 1000)
    public void onMotion(MotionEvent event) {
        if (!event.isPre()) return;
        
        if (mc.player.onGround) {
            mc.player.offGroundTicks = 0;
            mc.player.onGroundTicks++;
        } else {
            mc.player.onGroundTicks = 0;
            mc.player.offGroundTicks++;
        }
    }
}
