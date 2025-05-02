package loftily.module.impl.render;

import loftily.Client;
import loftily.event.impl.render.Render2DEvent;
import loftily.gui.font.FontManager;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;

@ModuleInfo(name = "ModuleList", category = ModuleCategory.Render)
public class ModuleList extends Module {
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        int y = 0;
        for (Module module : Client.INSTANCE.getModuleManager()) {
            if (!module.isToggled()) continue;
            FontManager.NotoSans.of(18).drawStringWithShadow(
                    module.getName(),
                    event.getScaledResolution().getScaledWidth() - FontManager.NotoSans.of(18).getStringWidth(module.getName()) - 2,
                    2 + y,
                    -1);
            y += 10;
        }
    }
}
