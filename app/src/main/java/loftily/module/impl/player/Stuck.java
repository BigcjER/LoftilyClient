package loftily.module.impl.player;


import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.event.impl.packet.PacketSendEvent;
import loftily.event.impl.world.UpdateEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;


@ModuleInfo(name = "Stuck", category = ModuleCategory.PLAYER)
public class Stuck extends Module {
    private final BooleanValue autoDisable = new BooleanValue("AutoDisable", false);
    private final BooleanValue resetPosition = new BooleanValue("ResetPositionOnFlag", false);
    private final BooleanValue lastMotion = new BooleanValue("LastMotion", false);

    
    private double x, y, z;
    private double motionX, motionY, motionZ;
    
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

        this.motionX = mc.player.motionX;
        this.motionY = mc.player.motionY;
        this.motionZ = mc.player.motionZ;
    }

    @Override
    public void onDisable() {
        if(lastMotion.getValue()) {
            mc.player.motionX = motionX;
            mc.player.motionY = motionY;
            mc.player.motionZ = motionZ;
        }
    }

    @EventHandler
    public void onPacket(PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketPlayerPosLook && autoDisable.getValue()) {
            if (resetPosition.getValue()) {
                x = ((SPacketPlayerPosLook) packet).getX();
                y = ((SPacketPlayerPosLook) packet).getY();
                z = ((SPacketPlayerPosLook) packet).getZ();
            }
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
