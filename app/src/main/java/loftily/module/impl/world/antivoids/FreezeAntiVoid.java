package loftily.module.impl.world.antivoids;

import loftily.Client;
import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.module.impl.player.Scaffold;
import loftily.module.impl.world.AntiVoid;
import loftily.utils.client.PacketUtils;
import loftily.utils.player.PlayerUtils;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRespawn;

import java.util.LinkedList;
import java.util.Queue;

public class FreezeAntiVoid extends Mode<AntiVoid> {
    public FreezeAntiVoid() {
        super("Freeze");
    }
    private double prevX, prevY, prevZ;
    private final Queue<Packet<?>> packets = new LinkedList<>();

    @EventHandler
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if(Client.INSTANCE.getModuleManager().get(Scaffold.class).isToggled())return;

        if (packet instanceof CPacketPlayer) {
            if (PlayerUtils.isInVoid()) {
                if(mc.player.motionY <= 0) {
                    event.setCancelled(true);
                    packets.add(packet);
                    if (!getParent().isSafe()) {
                        PacketUtils.sendPacket(new CPacketPlayer.Position(prevX, prevY - 1, prevZ, false), false);
                    }
                }
            } else {
                if (mc.player.onGround) {
                    prevX = mc.player.prevPosX;
                    prevY = mc.player.prevPosY;
                    prevZ = mc.player.prevPosZ;
                    while (!packets.isEmpty()) {
                        Packet<?> packetPos = packets.poll();
                        PacketUtils.sendPacket(packetPos,false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if(!packets.isEmpty()){
            if(packet instanceof SPacketPlayerPosLook || packet instanceof SPacketRespawn){
                packets.clear();
            }
        }
    }

    @Override
    public void onDisable(){
        packets.clear();
    }
}
