package loftily.module.impl.movement.flys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.client.PacketUtils;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class TatakoFly extends Mode<Fly> {
    private final NumberValue horizontalSpeed = new NumberValue("Tatako-HorizontalSpeed", 1, 0, 5, 0.01);
    private final NumberValue verticalSpeed = new NumberValue("Tatako-VerticalSpeed", 1, 0, 5, 0.01);
    private double x, y, z;
    
    public TatakoFly() {
        super("Tatako");
    }
    
    @Override
    public void onEnable() {
        x = mc.player.posX;
        y = mc.player.posY;
        z = mc.player.posZ;
    }
    
    @Override
    public void onDisable() {
        EntityPlayer player = mc.player;
        PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(player.posX + 0.05, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, true));
        PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(player.posX, player.posY + 0.42, player.posZ, player.rotationYaw, player.rotationPitch, true));
        PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(player.posX, player.posY + 0.7532, player.posZ, player.rotationYaw, player.rotationPitch, true));
        PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(player.posX, player.posY + 1.0, player.posZ, player.rotationYaw, player.rotationPitch, true));
    }
    
    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        MoveUtils.stop(true);
        
        MoveUtils.setSpeed(horizontalSpeed.getValue(), true);
        
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) {
            mc.player.motionY = verticalSpeed.getValue();
        }
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.player.motionY = -verticalSpeed.getValue();
        }
    }
    
    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketPlayer) {
            y -= 0.09;
            ((CPacketPlayer) packet).x = x;
            ((CPacketPlayer) packet).y = y;
            ((CPacketPlayer) packet).z = z;
            ((CPacketPlayer) packet).onGround = true;
        }
    }
    
    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketPlayerPosLook) {
            x = ((SPacketPlayerPosLook) packet).getX();
            y = ((SPacketPlayerPosLook) packet).getY();
            z = ((SPacketPlayerPosLook) packet).getZ();
            PacketUtils.sendPacket(new CPacketConfirmTeleport(((SPacketPlayerPosLook) packet).getTeleportId()));
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(x, y, z, ((SPacketPlayerPosLook) packet).getYaw(), ((SPacketPlayerPosLook) packet).getPitch(), false));
            event.setCancelled(true);
        }
    }
}
