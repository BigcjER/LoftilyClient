package loftily.module.impl.player;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.impl.client.BlinkHandler;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.PacketUtils;
import loftily.utils.math.RandomUtils;
import loftily.utils.timer.DelayTimer;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.LinkedList;
import java.util.Queue;

@ModuleInfo(name = "Blink", category = ModuleCategory.PLAYER)
public class Blink extends Module {
    private final BooleanValue noC0F = new BooleanValue("NoCOF", false);
    private final BooleanValue noC00 = new BooleanValue("NoCO0", false);
    private final BooleanValue serverPackets = new BooleanValue("SPackets", false);

    public final Queue<Packet<?>> packetBus = new LinkedList<>();

    private final ModeValue pulseMode = new ModeValue("PulseMode", "None", this,
            new StringMode("None"),
            new StringMode("CustomSize"),
            new StringMode("Normal"));
    
    private final RangeSelectionNumberValue pulseSize =
            new RangeSelectionNumberValue("PulseSize", 200, 400, 0, 2000).
                    setVisible(() -> pulseMode.is("CustomSize"));
    
    private final RangeSelectionNumberValue pulseDelay =
            new RangeSelectionNumberValue("PulseDelay", 200, 400, 0, 10000).
                    setVisible(() -> !pulseMode.is("None"));
    
    private final BooleanValue fakePlayer = new BooleanValue("FakePlayer", false);
    
    public static final int FAKE_ENTITY_ID = -1000;
    private EntityOtherPlayerMP fakePlayerEntity;
    
    private final DelayTimer pulseTimer = new DelayTimer();
    private int delay;

    public void releasePackets(){
        if(packetBus.isEmpty() || !serverPackets.getValue()){
            return;
        }
        for (Packet<?> packet : packetBus) {
            PacketUtils.receivePacket(packet,false);
        }
        packetBus.clear();
    }

    @Override
    public void onEnable() {
        BlinkHandler.setBlinkState(true, noC0F.getValue(), noC00.getValue(), false);
        if (fakePlayer.getValue()) {
            fakePlayerEntity = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
            fakePlayerEntity.rotationYawHead = mc.player.rotationYawHead;
            fakePlayerEntity.copyLocationAndAnglesFrom(mc.player);
            mc.world.addEntityToWorld(FAKE_ENTITY_ID, fakePlayerEntity);
        }
        pulseTimer.reset();
        delay = RandomUtils.randomInt((int) pulseDelay.getFirst(), (int) pulseDelay.getSecond());
    }

    @EventHandler
    public void onReceivePacket(PacketReceiveEvent event){
        Packet<?> packet = event.getPacket();
        if(serverPackets.getValue()){
            event.setCancelled(true);
            packetBus.add(packet);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (pulseMode.is("None")) return;
        
        if (pulseTimer.hasTimeElapsed(delay)) {
            pulseTimer.reset();
            delay = RandomUtils.randomInt((int) pulseDelay.getFirst(), (int) pulseDelay.getSecond());
            switch (pulseMode.getValue().getName()) {
                case "Normal":
                    BlinkHandler.setBlinkState(BlinkHandler.BLINK, BlinkHandler.BLINK_NOC0F, BlinkHandler.BLINK_NOC00, true);
                    releasePackets();
                    if (fakePlayer.getValue()) {
                        if (fakePlayerEntity != null) {
                            fakePlayerEntity.copyLocationAndAnglesFrom(mc.player);
                            fakePlayerEntity.rotationYawHead = mc.player.rotationYawHead;
                        }
                    }
                    break;
                case "CustomSize":
                    int i = 0;
                    int size = RandomUtils.randomInt((int) pulseSize.getFirst(), (int) pulseSize.getSecond());

                    while (!BlinkHandler.packets.isEmpty()) {
                        if (i >= size) return;
                        
                        Packet<?> packet = BlinkHandler.packets.poll();
                        PacketUtils.sendPacket(packet, false);
                        i++;
                        
                        if (fakePlayerEntity != null && packet instanceof CPacketPlayer && ((CPacketPlayer) packet).getMoving()) {
                            if (((CPacketPlayer) packet).getRotating()) {
                                fakePlayerEntity.rotationYawHead = ((CPacketPlayer) packet).yaw;
                                fakePlayerEntity.rotationPitch = ((CPacketPlayer) packet).pitch;
                            }
                            fakePlayerEntity.setPosition(((CPacketPlayer) packet).getX(0), ((CPacketPlayer) packet).getY(0), ((CPacketPlayer) packet).getZ(0));
                        }
                    }

                    while (!packetBus.isEmpty()) {
                        if (i >= size) return;

                        Packet<?> packet = packetBus.poll();
                        PacketUtils.receivePacket(packet,false);
                        i++;
                    }
                    break;
            }
        }
    }
    
    @Override
    public void onDisable() {
        BlinkHandler.setBlinkState(false, false, false, true);
        if (fakePlayer.getValue() && fakePlayerEntity != null) {
            mc.world.removeEntityFromWorld(fakePlayerEntity.getEntityId());
            fakePlayerEntity = null;
        }
        releasePackets();
    }
}