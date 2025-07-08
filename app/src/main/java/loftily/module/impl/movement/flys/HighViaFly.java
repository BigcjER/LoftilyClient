package loftily.module.impl.movement.flys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.client.PacketUtils;
import loftily.utils.player.MoveUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.util.LinkedList;
import java.util.Queue;

public class HighViaFly extends Mode<Fly> {
    public final Queue<Packet<?>> packetBus = new LinkedList<>();
    /*Only works in high version server!!!
     *Bypass grim,matrix and most other AC!!!
     */
    private final NumberValue horizontalSpeed = new NumberValue("HorizontalSpeed", 1, 0, 5, 0.01);
    private final NumberValue verticalSpeed = new NumberValue("VerticalSpeed", 1, 0, 5, 0.01);
    private final BooleanValue noClip = new BooleanValue("NoClip", false);
    //private final NumberValue ySpeed = new NumberValue("YSpeed",2,0,10,0.01);
    
    private final NumberValue packets = new NumberValue("OffsetPackets", 1, 0, 10);
    private final NumberValue yPackets = new NumberValue("InvalidYPackets", 1, 0, 10);
    private final NumberValue teleportSpeed = new NumberValue("TeleportSpeed", 11, 0, 999);
    
    public HighViaFly() {
        super("HighViaPacket");
    }
    
    @EventHandler
    public void onReceivePacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketPlayerPosLook) {
            event.setCancelled(true);
            packetBus.add(packet);
        }
    }
    
    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketPlayer.PositionRotation) {
            event.setCancelled(true);
            PacketUtils.sendPacket(new CPacketPlayer.Position(((CPacketPlayer.PositionRotation) packet).x,
                    ((CPacketPlayer.PositionRotation) packet).y,
                    ((CPacketPlayer.PositionRotation) packet).z,
                    ((CPacketPlayer.PositionRotation) packet).onGround));
        }
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
        
        if (noClip.getValue()) {
            mc.player.noClip = true;
        }
        for (int i = 0; i < packets.getValue(); i++) {
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(
                    mc.player.posX + 999,
                    mc.player.posY,
                    mc.player.posZ + 999,
                    mc.player.rotationYaw,
                    mc.player.rotationPitch, false
            ));
        }
        for (int i = 0; i < yPackets.getValue(); i++) {
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(
                    mc.player.posX + 999,
                    mc.player.posY - 50,
                    mc.player.posZ + 999,
                    mc.player.rotationYaw,
                    mc.player.rotationPitch, false
            ));
        }
        
        if (teleportSpeed.getValue() != 0) {
            mc.player.setPosition(
                    mc.player.posX + mc.player.motionX * teleportSpeed.getValue(),
                    mc.player.posY,
                    mc.player.posZ + mc.player.motionZ * teleportSpeed.getValue()
            );
        }
    }
    
    @Override
    public void onDisable() {
        mc.player.capabilities.isFlying = false;
        mc.player.capabilities.setFlySpeed(0.05F);
        if (!packetBus.isEmpty()) {
            for (Packet<?> packet : packetBus) {
                PacketUtils.receivePacket(packet, false);
            }
            packetBus.clear();
        }
        mc.player.noClip = false;
    }
}
