package loftily.module.impl.player;


import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.Rotation;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;


@ModuleInfo(name = "Stuck", category = ModuleCategory.PLAYER)
public class Stuck extends Module {
    private final BooleanValue autoDisable = new BooleanValue("AutoDisable", false);
    
    private double x, y, z;
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (isToggled() && autoDisable.getValue()) setToggled(false, true, true);
    }
    
    @Override
    public void onEnable() {
        if (mc.player == null) return;
        
        this.x = mc.player.posX;
        this.y = mc.player.posY;
        this.z = mc.player.posZ;
    }
    
    @EventHandler
    public void onPacket(PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook && autoDisable.getValue()) {
            toggle();
        }
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        mc.player.motionX = 0.0;
        mc.player.motionY = 0.0;
        mc.player.motionZ = 0.0;
        mc.player.setPosition(this.x, this.y, this.z);
    }
}
