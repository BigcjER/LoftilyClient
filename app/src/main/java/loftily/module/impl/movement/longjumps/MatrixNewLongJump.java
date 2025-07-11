package loftily.module.impl.movement.longjumps;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.movement.LongJump;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class MatrixNewLongJump extends Mode<LongJump> {
    public MatrixNewLongJump() {
        super("MatrixNew");
    }
    private final ModeValue modeValue = new ModeValue("BypassMethod","Fall",this,
            new StringMode("Fall"),new StringMode("NoGround")
    );
    private final NumberValue boostSpeed = new NumberValue("Matrix-BoostSpeed", 2.1, -3.0, 8.0, 0.01);

    private boolean receivedFlag, canBoost, boosted,touchGround;
    @Override
    public void onEnable(){
        boosted = false;
        canBoost = false;
        receivedFlag = false;
        touchGround = false;
        if(modeValue.is("NoGround")){
            if(mc.player.onGround){
                mc.player.tryJump();
            }
            touchGround = true;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event){
        Packet<?> packet = event.getPacket();
        if(packet instanceof SPacketPlayerPosLook){
            receivedFlag = true;
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event){
        if(modeValue.is("NoGround") && !canBoost){
            event.setOnGround(false);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event){
        if(!mc.player.onGround && touchGround){
            touchGround = false;
        }
        if(mc.player.onGround && !touchGround) {
            getParent().jump();
            boosted = false;
            if(modeValue.is("NoGround") && !boosted){
                canBoost = true;
            }
        }
        if(mc.player.fallDistance >= 0.25 && !boosted && modeValue.is("Fall")){
            canBoost = true;
        }
        if(canBoost){
            MoveUtils.setSpeed(boostSpeed.getValue(), false);
            mc.player.motionY = 0.42;
            boosted = true;
        }
        if(receivedFlag && boosted){
            getParent().autoDisable();
            canBoost = false;
            receivedFlag = false;
        }
    }
}
