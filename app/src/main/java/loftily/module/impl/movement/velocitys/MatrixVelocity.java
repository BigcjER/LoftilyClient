package loftily.module.impl.movement.velocitys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.handlers.impl.player.CombatHandler;
import loftily.module.impl.movement.Velocity;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class MatrixVelocity extends Mode<Velocity> {
    
    public MatrixVelocity() {
        super("Matrix");
    }
    private final BooleanValue strafe = new BooleanValue("Matrix-HurtStrafe",false);
    private final RangeSelectionNumberValue strafeHurtTime = new RangeSelectionNumberValue("StrafeHurtTime",6,10,1,10);
    private final BooleanValue boost = new BooleanValue("Matrix-Boost",false);
    private final NumberValue boostSpeed = new NumberValue("Matrix-BoostSpeed",0,0.1,0.7,0.01);

    @EventHandler
    public void onStrafe(StrafeEvent event){
        if(CombatHandler.inCombat && strafe.getValue()){
            if(mc.player.hurtTime >= strafeHurtTime.getFirst() && mc.player.hurtTime <= strafeHurtTime.getSecond()){
                if(boost.getValue()) {
                    MoveUtils.setSpeed(boostSpeed.getValue(), true);
                }else {
                    MoveUtils.strafe();
                }
            }
        }
    }

    @EventHandler
    public void onReceivePacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketEntityVelocity) {
            if (((SPacketEntityVelocity) packet).getEntityID() == mc.player.getEntityId()) {
                event.setCancelled(true);
                if (((SPacketEntityVelocity) packet).getMotionY() / 8000f > 0.22) {
                    mc.player.motionY = ((SPacketEntityVelocity) packet).getMotionY() / 8000f;
                    if (!MoveUtils.isMoving()) {
                        MoveUtils.setSpeed(Math.max(
                                MoveUtils.getSpeed(
                                        ((SPacketEntityVelocity) packet).getMotionX() / 8000f,
                                        ((SPacketEntityVelocity) packet).getMotionZ() / 8000f) * 0.1,
                                MoveUtils.getSpeed()), false);
                    } else {
                        MoveUtils.strafe();
                    }
                }
            }
        }
    }
}
