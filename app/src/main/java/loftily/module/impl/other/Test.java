package loftily.module.impl.other;

import loftily.event.impl.player.motion.MoveEvent;
import loftily.module.AutoDisableType;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.entity.EntityPlayerSP;

@SuppressWarnings("unused")
@ModuleInfo(name = "Test", category = ModuleCategory.OTHER, autoDisable = AutoDisableType.FLAG)
public class Test extends Module {
    
    @EventHandler
    public void onMove(MoveEvent event) {
        if (event.getEntity() instanceof EntityPlayerSP && mc.player.onGround) {
            event.setSafeWalk(true);
        }
    }
}
