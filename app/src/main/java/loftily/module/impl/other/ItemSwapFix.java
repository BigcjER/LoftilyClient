package loftily.module.impl.other;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketHeldItemChange;

@ModuleInfo(name = "ItemSwapFix",category = ModuleCategory.OTHER)
public class ItemSwapFix extends Module {
    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if(packet instanceof SPacketHeldItemChange){
            event.setCancelled(true);
        }
    }
}
