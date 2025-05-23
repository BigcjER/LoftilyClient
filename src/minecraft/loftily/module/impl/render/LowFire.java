package loftily.module.impl.render;

import loftily.event.impl.render.RenderFireEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.NumberValue;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "LowFire", category = ModuleCategory.Render)
public class LowFire extends Module {
    public final NumberValue fireTranslateY = new NumberValue("FireTranslateY", -0.5F, -1F, 0, 0.1F);
    
    @EventHandler
    public void onRenderFire(RenderFireEvent event) {
        event.setTranslateY(fireTranslateY.getValue().floatValue());
    }
}