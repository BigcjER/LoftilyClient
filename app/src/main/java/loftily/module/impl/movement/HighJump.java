package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;

@ModuleInfo(name = "HighJump", category = ModuleCategory.MOVEMENT)
public class HighJump extends Module {
    private final ModeValue modeValue = new ModeValue("Mode", "Vanilla", this,
            new StringMode("Vanilla"),
            new StringMode("Matrix")
    );
    private final NumberValue motion = new NumberValue("Motion", 0.8, 0.0, 10.0, 0.01).setVisible(()->modeValue.is("Vanilla"));
    private final BooleanValue autoToggle = new BooleanValue("AutoToggle", false);
    private final DelayTimer delayTimer = new DelayTimer();
    
    boolean active, falling;
    int ticksSinceJump;

    public void runToggle() {
        if (autoToggle.getValue()) {
            toggle();
        }
    }

    @Override
    public void onEnable() {
        ticksSinceJump = 0;
        active = falling = false;
    }

    @Override
    public void onDisable() {
        delayTimer.reset();
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
        if(packet instanceof SPacketEntityVelocity){
            SPacketEntityVelocity velocity = (SPacketEntityVelocity) packet;
            if(velocity.getEntityID() == mc.player.getEntityId() && velocity.getMotionY() < -500){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (modeValue.is("Matrix")) {
            if (mc.player.isCollidedVertically) {
                active = true;
            }
            
            if (ticksSinceJump == 1) {
                mc.player.onGround = false;
                mc.player.motionY = 0.998D;
            }
            
            if (mc.player.isCollidedVertically && this.ticksSinceJump > 4) {
                runToggle();
            }
            
            if (!mc.player.onGround && ticksSinceJump >= 2) {
                mc.player.motionY += 0.0034999D;
                if (!falling && mc.player.motionY < 0.0D && mc.player.motionY > -0.05D) {
                    mc.player.motionY = 0.0029999D;
                    falling = true;
                    runToggle();
                }
            }
            
            if (active) {
                ++ticksSinceJump;
            }
        }
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (modeValue.getValue().getName().equals("Vanilla")) {
            event.setCancelled(true);
            mc.player.motionY = motion.getValue();
            runToggle();
        }
    }
}
