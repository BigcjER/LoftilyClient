package loftily.module.impl.movement.flys;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.PreUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.util.LinkedList;
import java.util.Queue;

public class HighViaFly extends Mode<Fly> {
    public HighViaFly() {
        super("HighViaPacket");
    }
    /*Only works in high version server!!!
     *Bypass grim,matrix and most other AC!!!
     */
    private final NumberValue xzSpeed = new NumberValue("XZSpeed",2,0,10,0.01);
    //private final NumberValue ySpeed = new NumberValue("YSpeed",2,0,10,0.01);

    private final NumberValue packets = new NumberValue("OffsetPackets",1,0,10);
    private final NumberValue yPackets = new NumberValue("InvalidYPackets",1,0,10);
    private final NumberValue teleportSpeed = new NumberValue("TeleportSpeed",11,0,999);

    public final Queue<Packet<?>> packetBus = new LinkedList<>();

    @EventHandler
    public void onReceivePacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof SPacketPlayerPosLook) {
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
        mc.player.capabilities.isFlying = true;
        mc.player.capabilities.setFlySpeed(xzSpeed.getValue().floatValue());

        for (int i = 0; i < yPackets.getValue(); i++) {
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(
                    mc.player.posX + 999,
                    mc.player.posY - 6969,
                    mc.player.posZ + 999,
                    mc.player.rotationYaw,
                    mc.player.rotationPitch,true
            ));
        }
        for (int i = 0; i < packets.getValue(); i++) {
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(
                    mc.player.posX + 999,
                    mc.player.posY + (mc.gameSettings.keyBindJump.isKeyDown() ? 1.5624 : 0.00000001) - (mc.gameSettings.keyBindSneak.isKeyDown() ? 0.0624 : 0.00000002),
                    mc.player.posZ + 999,
                    mc.player.rotationYaw,
                    mc.player.rotationPitch,true
            ));
        }

        if(teleportSpeed.getValue() != 0) {
            mc.player.setPosition(
                    mc.player.posX + mc.player.motionX * teleportSpeed.getValue(),
                    mc.player.posY,
                    mc.player.posZ + mc.player.motionZ * teleportSpeed.getValue()
            );
        }
        mc.player.motionY = 0.0;
    }

    @Override
    public void onDisable() {
        mc.player.capabilities.isFlying = false;
        mc.player.capabilities.setFlySpeed(0.05F);
        if(!packetBus.isEmpty()) {
            for (Packet<?> packet : packetBus) {
                PacketUtils.receivePacket(packet, false);
            }
            packetBus.clear();
        }
    }
}
