package loftily.module.impl.player;

import loftily.event.impl.world.UpdateEvent;
import loftily.handlers.impl.BlinkHandler;
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

@ModuleInfo(name = "Blink",category = ModuleCategory.Player)
public class Blink extends Module {
    private final BooleanValue noC0F = new BooleanValue("NoCOF",false);
    private final BooleanValue noC00 = new BooleanValue("NoCO0",false);

    private final ModeValue pulseMode = new ModeValue("PulseMode","None",this,
            new StringMode("None"),
            new StringMode("CustomSize"),
            new StringMode("Normal"));

    private final RangeSelectionNumberValue pulseSize =
            new RangeSelectionNumberValue("PulseSize", 200,400,0,2000).
                    setVisible(()->pulseMode.is("CustomSize"));

    private final RangeSelectionNumberValue pulseDelay =
            new RangeSelectionNumberValue("PulseDelay", 200,400,0,10000).
                    setVisible(()->!pulseMode.is("None"));

    private final BooleanValue fakePlayer = new BooleanValue("FakePlayer",false);

    private EntityOtherPlayerMP model;
    private final DelayTimer pulseTimer = new DelayTimer();
    private int delay;

    @Override
    public void onEnable() {
        BlinkHandler.setBlinkState(true,noC0F.getValue(),noC00.getValue(),false);
        if(fakePlayer.getValue()) {
            model = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
            model.rotationYawHead = mc.player.rotationYawHead;
            model.copyLocationAndAnglesFrom(mc.player);
            mc.world.addEntityToWorld(-1000, model);
        }
        pulseTimer.reset();
        delay = RandomUtils.randomInt((int)pulseDelay.getFirst(),(int)pulseDelay.getSecond());
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(pulseMode.is("None"))return;

        if(pulseTimer.hasTimeElapsed(delay)){
            pulseTimer.reset();
            delay = RandomUtils.randomInt((int)pulseDelay.getFirst(),(int)pulseDelay.getSecond());
            switch (pulseMode.getValue().getName()){
                case "Normal":
                    BlinkHandler.setBlinkState(BlinkHandler.BLINK,BlinkHandler.BLINK_NOC0F,BlinkHandler.BLINK_NOC00,true);
                    if(fakePlayer.getValue()) {
                        mc.world.removeEntityFromWorld(model.getEntityId());
                        model = null;
                        model = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
                        model.rotationYawHead = mc.player.rotationYawHead;
                        model.copyLocationAndAnglesFrom(mc.player);
                        mc.world.addEntityToWorld(-1000, model);
                    }
                    break;
                case "CustomSize":
                    int i = 0;
                    int size = RandomUtils.randomInt((int)pulseSize.getFirst(),(int)pulseSize.getSecond());

                    for (Packet<?> packet : BlinkHandler.packets) {
                        if(i >= size || BlinkHandler.packets.isEmpty()){
                            return;
                        }
                        if (packet != null) {
                            PacketUtils.sendPacket(packet,false);
                            BlinkHandler.packets.remove(packet);
                            i++;
                        }

                        if(packet instanceof CPacketPlayer && ((CPacketPlayer) packet).getMoving()){
                            if(((CPacketPlayer) packet).getRotating()) {
                                model.setPositionAndRotation(((CPacketPlayer) packet).getX(0),
                                        ((CPacketPlayer) packet).getY(0),
                                        ((CPacketPlayer) packet).getZ(0),
                                        ((CPacketPlayer) packet).yaw,
                                        ((CPacketPlayer) packet).pitch);
                                //model.setAngles(((CPacketPlayer) packet).yaw,((CPacketPlayer) packet).pitch);
                                model.rotationYawHead = ((CPacketPlayer) packet).yaw;
                                model.rotationPitch = ((CPacketPlayer) packet).pitch;
                            }
                            model.setPosition(((CPacketPlayer) packet).getX(0),((CPacketPlayer) packet).getY(0),((CPacketPlayer) packet).getZ(0));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onDisable() {
        BlinkHandler.setBlinkState(false,false,false,true);
        if(fakePlayer.getValue()) {
            mc.world.removeEntityFromWorld(model.getEntityId());
            model = null;
        }
    }
}
