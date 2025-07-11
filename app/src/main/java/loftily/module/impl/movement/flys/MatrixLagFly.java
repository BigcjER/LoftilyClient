package loftily.module.impl.movement.flys;

import loftily.event.impl.client.ClientTickEvent;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.player.motion.MotionEvent;
import loftily.event.impl.player.motion.StrafeEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.impl.client.FlagHandler;
import loftily.module.impl.movement.Fly;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class MatrixLagFly extends Mode<Fly> {
    public MatrixLagFly() {
        super("MatrixLag");
    }
    private final BooleanValue zeroRot = new BooleanValue("ZeroRotation",false);
    private final NumberValue minFlags = new NumberValue("MinFlags",1,1,5);
    private boolean canJump = false;
    private boolean flag = false;
    private int flags = 0;

    @Override
    public void onToggle(){
        canJump = false;
        flag = false;
        mc.timer.timerSpeed = 1;
    }

    @Override
    public void onEnable(){
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (canJump) {
            mc.timer.timerSpeed = 1;
            mc.player.motionY = 0.42;
            canJump = false;
        } else if (flag) {
            if (mc.player.motionY < -0.01) {
                mc.timer.timerSpeed = 1;
                flag = false;
            }
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if(!flag){
            if(event.isPre()) {
                PacketUtils.sendPacket(new CPacketPlayer.Position(mc.player.posX + 16, mc.player.posY, mc.player.posZ + 16, false));
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event){
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketPlayer) {
            if(!zeroRot.getValue()){
                return;
            }
            CPacketPlayer packetPlayer = (CPacketPlayer) packet;
            if(packetPlayer.getMoving()){
                PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(packetPlayer.x,packetPlayer.y,packetPlayer.z,0.0f,0.0f,packetPlayer.onGround),false);
            }else {
                PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX,mc.player.posY,mc.player.posZ,0.0f,0.0f,packetPlayer.onGround),false);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof SPacketPlayerPosLook){
            flags++;
            FlagHandler.flagSilentMotion = true;
            if(flags >= minFlags.getValue()) {
                if (!canJump && !flag) {
                    canJump = true;
                    flag = true;
                }
            }
        }
    }
}
