package loftily.utils.client;

import net.minecraft.network.Packet;

public class PacketUtils implements ClientUtils {
    public static void sendPacket(Packet<?> packet) {
        if (mc.player != null) mc.player.connection.sendPacket(packet);
    }
}
