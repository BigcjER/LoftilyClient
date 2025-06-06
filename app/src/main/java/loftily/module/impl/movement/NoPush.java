package loftily.module.impl.movement;

import loftily.event.impl.player.PushEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "NoPush", category = ModuleCategory.MOVEMENT)
public class NoPush extends Module {
    @EventHandler
    public void onPush(PushEvent event) {
        event.setCancelled(true);
    }
}
