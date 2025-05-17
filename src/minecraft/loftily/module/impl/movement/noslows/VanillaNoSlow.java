package loftily.module.impl.movement.noslows;

import loftily.event.impl.player.slowdown.ItemSlowDownEvent;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class VanillaNoSlow extends Mode {
    public VanillaNoSlow(String name) {
        super(name);
    }
    
    @EventHandler
    public void onSlowDown(ItemSlowDownEvent event) {
        event.setCancelled(true);
    }
}
