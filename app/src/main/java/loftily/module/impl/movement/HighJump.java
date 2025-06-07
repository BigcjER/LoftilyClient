package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.JumpEvent;
import loftily.event.impl.world.UpdateEvent;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@ModuleInfo(name = "HighJump",category = ModuleCategory.MOVEMENT)
public class HighJump extends Module {
    private final ModeValue modeValue = new ModeValue("Mode","Vanilla",this,
            new StringMode("Vanilla"),
            new StringMode("Test")
    );
    private final NumberValue motion = new NumberValue("Motion",0.8,0.0,10.0,0.01);
    private final BooleanValue autoToggle = new BooleanValue("AutoToggle",false);
    private final DelayTimer delayTimer = new DelayTimer();


    public void runToggle(){
        if(autoToggle.getValue()){
            this.toggle();
        }
    }

    @Override
    public void onDisable(){
        delayTimer.reset();
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (modeValue.is("Test")) {
            if (delayTimer.hasTimeElapsed(350)) {
                mc.player.jump();
                mc.player.motionY += motion.getValue();
                runToggle();
                delayTimer.reset();
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
