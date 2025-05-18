package loftily.handlers.impl;

import loftily.event.impl.packet.PacketSendEvent;
import loftily.handlers.Handler;
import loftily.utils.client.PacketUtils;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketKeepAlive;

import java.util.ArrayList;
import java.util.List;

public class BlinkHandler extends Handler {

    public static boolean BLINK = false;

    public static boolean BLINK_NOC0F = false;
    public static boolean BLINK_NOC00 = false;

    public static List<Packet<?>> packets = new ArrayList<>();

    public static void setBlinkState(
            boolean Blink,
            boolean noC0F,
            boolean noC00,
            boolean release
    ) {
        BLINK = Blink;
        BLINK_NOC0F = noC0F;
        BLINK_NOC00 = noC00;

        if(release && !packets.isEmpty()){
            for(Packet<?> packet : packets){
                PacketUtils.sendPacket(packet,false);
            }
            packets.clear();
        }
    }

    public static void releasePacketsCustom(int size){
        if(packets.isEmpty())return;

        int i = 0;

        for (Packet<?> packet : packets) {
            if(i >= size){
                return;
            }
            if (packet != null) {
                PacketUtils.sendPacket(packet,false);
                i++;
            }
        }
    }

    @EventHandler(priority = -1000)
    public void onPacketSend(PacketSendEvent event){
        Packet<?> packet = event.getPacket();

        if(!BLINK)return;

        if(BLINK_NOC00 && packet instanceof CPacketKeepAlive)return;

        if(BLINK_NOC0F && packet instanceof CPacketConfirmTransaction)return;

        event.setCancelled(true);
        packets.add(packet);
    }
}