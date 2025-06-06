package loftily.utils.client;

import net.minecraft.network.Packet;

public class PacketUtils implements ClientUtils {
    public static void sendPacket(Packet<?> packet) {
        sendPacket(packet, true);
    }
    
    public static void sendPacket(Packet<?> packet, boolean callEvent) {
        if (mc.player == null) return;
        if (callEvent)
            mc.player.connection.sendPacket(packet);
        else
            mc.player.connection.sendPacketNoEvent(packet);
    }
    
    public static void receivePacket(Packet<?> packet) {
        sendPacket(packet, true);
    }
    
    public static void receivePacket(Packet<?> packet, boolean callEvent) {
        if (mc.player == null) return;
        if (callEvent)
            mc.player.connection.getNetworkManager().receivePacket(packet);
        else {
            mc.player.connection.getNetworkManager().receivePacketNoEvent(packet);
        }
    }
}
