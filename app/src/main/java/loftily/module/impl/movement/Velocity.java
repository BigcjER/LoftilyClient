package loftily.module.impl.movement;

import loftily.event.impl.packet.PacketReceiveEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.client.ClassUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.ModeValue;
import lombok.NonNull;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketExplosion;

@ModuleInfo(name = "Velocity", category = ModuleCategory.MOVEMENT)
public class Velocity extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Normal", this,
            ClassUtils.getModes(getClass().getPackage().getName() + ".velocitys")
    );
    private final BooleanValue cancelExplosion = new BooleanValue("CancelExplosion", false);
    
    @EventHandler(priority = 9999)
    public void onReceivePacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketExplosion) {
            if (cancelExplosion.getValue()) {
                event.setCancelled(true);
            }
        }
    }
    
    @Override
    public @NonNull String getTag() {
        return mode.getValueByName();
    }
}
