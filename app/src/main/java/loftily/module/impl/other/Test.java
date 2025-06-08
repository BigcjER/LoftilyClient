package loftily.module.impl.other;

import loftily.event.impl.player.motion.MoveEvent;
import loftily.module.AutoDisableType;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.TextValue;
import net.lenni0451.lambdaevents.EventHandler;

@SuppressWarnings("unused")
@ModuleInfo(name = "Test", category = ModuleCategory.OTHER, autoDisable = AutoDisableType.FLAG)
public class Test extends Module {
    private final TextValue text = new TextValue("Text", "123333333333");
    
    @EventHandler
    public void onMove(MoveEvent event) {
        println(text.getValue());
    }
}
