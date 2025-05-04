package loftily.module.impl.render;

import loftily.Client;
import loftily.event.impl.render.Render2DEvent;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import net.lenni0451.lambdaevents.EventHandler;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ModuleInfo(name = "ModuleList", category = ModuleCategory.Render)
public class ModuleList extends Module {
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (mc.gameSettings.showDebugInfo) return;
        FontRenderer font = FontManager.NotoSans.of(18);
        List<Module> modules = Client.INSTANCE.getModuleManager().stream()
                .sorted(Comparator.comparingInt(module -> -font.getStringWidth(module.getName() + " §f" + module.getTag())))
                .collect(Collectors.toList());
        
        int y = 0;
        for (Module module : modules) {
            if (!module.isToggled()) continue;
            font.drawStringWithShadow(
                    module.getName(),
                    event.getScaledResolution().getScaledWidth() - font.getStringWidth(module.getName()) - 2,
                    2 + y,
                    -1);
            y += 10;
        }
    }
}
