package loftily.module.impl.other;

import loftily.event.impl.world.UpdateEvent;
import loftily.value.impl.mode.Mode;
import net.lenni0451.lambdaevents.EventHandler;

public class Mode2 extends Mode {
    public Mode2(String name) {
        super(name);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        System.out.println("Mode2");
    }
}
