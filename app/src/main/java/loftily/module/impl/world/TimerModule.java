package loftily.module.impl.world;

import loftily.event.impl.world.UpdateEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;
import lombok.NonNull;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "Timer", category = ModuleCategory.WORLD)
public class TimerModule extends Module {
    private final NumberValue timerSpeed = new NumberValue("TimerSpeed", 1, 0.01, 50, 0.01);
    
    @Override
    public void onDisable() {
        if (mc.timer.timerSpeed != 1)
            mc.timer.timerSpeed = 1.0f;
    }
    
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        mc.timer.timerSpeed = timerSpeed.getValue().floatValue();
    }
    
    @Override
    public @NonNull String getTag() {
        return String.valueOf(timerSpeed.getValue());
    }
}
