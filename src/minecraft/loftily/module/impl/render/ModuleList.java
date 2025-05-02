package loftily.module.impl.render;

import loftily.event.impl.render.Render2DEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ModuleInfo", key = Keyboard.KEY_V, category = ModuleCategory.Render)
public class ModuleList extends Module {
    @EventHandler
    public void onRender2D(Render2DEvent event) {
    }
}
