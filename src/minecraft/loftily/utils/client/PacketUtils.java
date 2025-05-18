package loftily.utils.client;

import net.minecraft.network.Packet;

public class PacketUtils implements ClientUtils {
    public static void sendPacket(Packet<?> packet) {
        if (mc.player != null) mc.player.connection.sendPacket(packet);
    }
    
    public static void sendPacket(Packet<?> packet, boolean callEvent) {
        if (mc.player == null) return;
        if (callEvent)
            mc.player.connection.sendPacket(packet);
        else
            mc.player.connection.sendPacketNoEvent(packet);
    }
}
