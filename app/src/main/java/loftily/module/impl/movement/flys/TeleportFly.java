package loftily.module.impl.movement.flys;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.LivingUpdateEvent;
import loftily.module.impl.movement.Fly;
import loftily.utils.client.PacketUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class TeleportFly extends Mode<Fly> {
    private final NumberValue packets = new NumberValue("PacketAmounts", 1, 1, 20);
    private final NumberValue speed = new NumberValue("Speed", 0.5, 0, 20, 0.01);
    private final BooleanValue tpWhenPacket = new BooleanValue("TPWhenPacketEvent", true);
    private final BooleanValue reSendPacket = new BooleanValue("ReSendWhenPacketEvent", false);
    private final NumberValue reSendAmounts = new NumberValue("ReSendAmounts", 1, 1, 20).setVisible(reSendPacket::getValue);
    private final BooleanValue spoofGround = new BooleanValue("SpoofGround", false);
    
    public TeleportFly() {
        super("TeleportFly");
    }
    
    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        double yaw = Math.toRadians(mc.player.rotationYaw);
        double pSpeed = speed.getValue();
        double x = mc.player.posX + (-sin(yaw) * pSpeed);
        double z = mc.player.posZ + (cos(yaw) * pSpeed);
        double y = mc.player.posY;
        for (int i = 0; i < packets.getValue(); i++) {
            PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(
                    x, y, z,
                    mc.player.rotationYaw, mc.player.rotationPitch, spoofGround.getValue()
            ), true);
        }
        if (!tpWhenPacket.getValue()) {
            mc.player.setPosition(x, y, z);
        }
    }
    
    @EventHandler
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof CPacketPlayer.PositionRotation) {
            double pSpeed = speed.getValue();
            double yaw = Math.toRadians(((CPacketPlayer.PositionRotation) packet).yaw);
            double x = ((CPacketPlayer.PositionRotation) packet).getX(mc.player.posX) + (-sin(yaw) * pSpeed);
            double z = ((CPacketPlayer.PositionRotation) packet).getZ(mc.player.posZ) + (cos(yaw) * pSpeed);
            double y = ((CPacketPlayer.PositionRotation) packet).getY(mc.player.posY);
            if (reSendPacket.getValue()) {
                for (int i = 0; i < reSendAmounts.getValue(); i++) {
                    PacketUtils.sendPacket(new CPacketPlayer.PositionRotation(
                            x, y, z,
                            mc.player.rotationYaw, mc.player.rotationPitch, spoofGround.getValue()
                    ), false);
                }
            }
            if (tpWhenPacket.getValue()) {
                mc.player.setPosition(((CPacketPlayer.PositionRotation) packet).getX(mc.player.posX),
                        ((CPacketPlayer.PositionRotation) packet).getY(mc.player.posY),
                        ((CPacketPlayer.PositionRotation) packet).getZ(mc.player.posZ));
            }
        }
    }
}
