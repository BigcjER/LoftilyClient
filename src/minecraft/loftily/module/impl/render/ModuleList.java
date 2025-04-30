package loftily.module.impl.render;

import loftily.Client;
import loftily.event.impl.client.KeyboardEvent;
import loftily.event.impl.render.Render2DEvent;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ModuleInfo",key = Keyboard.KEY_V,category = ModuleCategory.Render)
public class ModuleList extends Module {
    @EventHandler
    public void onRender2D(Render2DEvent event){
        int y = 0;
        for (Module module : Client.INSTANCE.getModuleManager()){
            if(module.isToggled()) {
                mc.fontRendererObj.drawStringWithShadow(module.getName(), 100, 100 + y, -1);
                y += 10;
            }
        }
    }
}
