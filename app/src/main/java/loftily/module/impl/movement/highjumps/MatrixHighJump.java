package loftily.module.impl.movement.highjumps;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.movement.HighJump;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class MatrixHighJump extends Mode<HighJump> {
    private boolean active;
    private boolean falling;
    private boolean moving;
    private int ticksSinceJump;

    public MatrixHighJump() {
        super("Matrix");
    }
    
    @Override
    public void onEnable() {
        ticksSinceJump = 0;
        active = falling = false;
        
        moving = MoveUtils.isMoving();
    }
    
    @EventHandler
    public void onMotion(MotionEvent event) {
        if (ticksSinceJump == 1) {
            event.setOnGround(false);
        }
    }
    
    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        
        if (packet instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocity = (SPacketEntityVelocity) packet;
            if (velocity.getEntityID() == mc.player.getEntityId() && velocity.getMotionY() < -500) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!moving) {
            MoveUtils.setSpeed(0.16, false);
            moving = true;
        }
        
        if (mc.player.isCollidedVertically) {
            active = true;
        }
        
        if (ticksSinceJump == 1) {
            mc.player.onGround = false;
            mc.player.motionY = 0.998D;
        }
        
        if (mc.player.isCollidedVertically && this.ticksSinceJump > 4) {
            getParent().autoDisable();
        }
        
        if (!mc.player.onGround && ticksSinceJump >= 2) {
            mc.player.motionY += 0.0034999D;
            if (!falling && mc.player.motionY < 0.0D && mc.player.motionY > -0.05D) {
                mc.player.motionY = 0.0029999D;
                falling = true;
                getParent().autoDisable();
            }
        }
        
        if (active) {
            ++ticksSinceJump;
        }
    }
}
