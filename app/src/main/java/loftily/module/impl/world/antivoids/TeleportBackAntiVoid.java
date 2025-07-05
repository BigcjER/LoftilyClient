package loftily.module.impl.world.antivoids;

import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.world.AntiVoid;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.block.BlockAir;
import net.minecraft.util.math.BlockPos;

public class TeleportBackAntiVoid extends Mode<AntiVoid> {
    private double prevX, prevY, prevZ;
    
    public TeleportBackAntiVoid() {
        super("TeleportBack");
    }
    
    @Override
    public void onToggle() {
        super.onToggle();
        
        prevX = prevY = prevZ = 0;
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player.onGround && (new BlockPos(mc.player).down()).getBlock() instanceof BlockAir) {
            prevX = mc.player.prevPosX;
            prevY = mc.player.prevPosY;
            prevZ = mc.player.prevPosZ;
        }
        
        if (getParent().isSafe()) return;
        
        mc.player.setPositionAndUpdate(prevX, prevY, prevZ);
        mc.player.fallDistance = 0F;
        mc.player.motionY = 0.0;
    }
}
