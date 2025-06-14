package loftily.handlers.impl.client;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.handlers.Handler;
import loftily.utils.client.PacketUtils;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketKeepAlive;

import java.util.LinkedList;
import java.util.Queue;

public class BlinkHandler extends Handler {
    
    public static boolean BLINK = false;
    
    public static boolean BLINK_NOC0F = false;
    public static boolean BLINK_NOC00 = false;
    
    public final static Queue<Packet<?>> packets = new LinkedList<>();
    
    public static void setBlinkState(
            boolean Blink,
            boolean noC0F,
            boolean noC00,
            boolean release
    ) {
        BLINK = Blink;
        BLINK_NOC0F = noC0F;
        BLINK_NOC00 = noC00;
        
        if (release) {
            while (!packets.isEmpty()) {
                Packet<?> packet = packets.poll();
                PacketUtils.sendPacket(packet, false);
            }
        }
    }
    
    public static void releasePacketsCustom(int size) {
        int i = 0;
        
        while (!packets.isEmpty() && i < size) {
            Packet<?> packet = packets.poll();
            if (packet != null) {
                PacketUtils.sendPacket(packet, false);
                i++;
            }
        }
    }
    
    @EventHandler(priority = -1000)
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        
        if (!BLINK) return;
        
        if (BLINK_NOC00 && packet instanceof CPacketKeepAlive) return;
        
        if (BLINK_NOC0F && packet instanceof CPacketConfirmTransaction) return;
        
        event.setCancelled(true);
        packets.add(packet);
    }
    
}