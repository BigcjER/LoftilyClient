package loftily.module.impl.movement.longjumps;

import loftily.Client;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.module.impl.movement.LongJump;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class MatrixLongJump extends Mode {
    public MatrixLongJump() {
        super("Matrix");
    }
    private final NumberValue boostSpeed = new NumberValue("Matrix-BoostSpeed",1.97,-1.97,1.97,0.01);
    private boolean flag = false;
    private boolean canBoost = false;
    private boolean boosted = false;

    @Override
    public void onDisable(){
        flag = false;
        canBoost = false;
    }

    @Override
    public void onEnable(){
        if(!mc.player.onGround && !boosted){
            canBoost = true;
        }
        boosted = false;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(mc.player.onGround){
            mc.player.tryJump();
            canBoost = true;
        }else {
            if(canBoost) {
                MoveUtils.setSpeed(boostSpeed.getValue(),false);
                mc.player.motionY = 0.42;
                boosted = true;
            }
        }
        if(flag && boosted) {
            Client.INSTANCE.getModuleManager().get(LongJump.class).toggle();
        }
    }

    @EventHandler
    public void onPacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof SPacketPlayerPosLook){
            flag = true;
        }
    }
}
