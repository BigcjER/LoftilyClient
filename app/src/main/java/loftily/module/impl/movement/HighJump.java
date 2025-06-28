package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

@ModuleInfo(name = "HighJump", category = ModuleCategory.MOVEMENT)
public class HighJump extends Module {
    private final ModeValue modeValue = new ModeValue("Mode", "Vanilla", this,
            new StringMode("Vanilla"),
            new StringMode("Test")
    );
    private final NumberValue motion = new NumberValue("Motion", 0.8, 0.0, 10.0, 0.01);
    private final BooleanValue autoToggle = new BooleanValue("AutoToggle", false);
    private final DelayTimer delayTimer = new DelayTimer();
    
    public void runToggle() {
        if (autoToggle.getValue()) {
            this.toggle();
        }
    }
    
    @Override
    public void onDisable() {
        delayTimer.reset();
    }
    
    @EventHandler
    public void onJump(JumpEvent event) {
        if (modeValue.getValue().getName().equals("Vanilla")) {
            event.setCancelled(true);
            mc.player.motionY = motion.getValue();
            runToggle();
        }
        if(modeValue.getValue().getName().equals("Test")) {
            event.setCancelled(true);
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(
                    mc.player.posX + 999,
                    mc.player.posY + motion.getValue(),
                    mc.player.posZ - 999,
                    mc.player.rotationYaw,
                    mc.player.rotationPitch,true
            ));
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(
                    mc.player.posX + 999,
                    mc.player.posY - 999,
                    mc.player.posZ - 999,
                    mc.player.rotationYaw,
                    mc.player.rotationPitch,true
            ));
            mc.player.motionY = motion.getValue();
        }
    }
}
