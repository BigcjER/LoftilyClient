package loftily.handlers.impl;

import loftily.event.impl.client.ClientTickEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.Handler;
import net.lenni0451.lambdaevents.EventHandler;

public class PlayerStateHandler extends Handler {
    @EventHandler(priority = 1000)
    public void onTick(ClientTickEvent event) {
        mc.player.movementYaw = mc.player.rotationYaw;
    }
    
    @EventHandler(priority = 1000)
    public void onUpdate(UpdateEvent event) {
        mc.player.prevRenderPitchHead = mc.player.renderPitchHead;
        mc.player.renderPitchHead = mc.player.rotationPitch;
    }
}
