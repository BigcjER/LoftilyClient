package loftily.module.impl.movement;

import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "Sprint", category = ModuleCategory.Movement)
public class Sprint extends Module {
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        mc.gameSettings.keyBindSprint.setPressed(true);
    }
}
