package loftily.module.impl.render;

import loftily.Client;
import loftily.event.impl.render.Render2DEvent;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.EasingModeValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ModuleInfo(name = "ModuleList", category = ModuleCategory.Render, defaultToggled = true)
public class ModuleList extends Module {
    
    private final List<ModuleEntry> moduleEntries = new ArrayList<>();
    
    private final NumberValue fontSize = new NumberValue("FontSize", 18, 10, 30);
    private final EasingModeValue moduleEasingMode = (EasingModeValue)
            new EasingModeValue("ModuleEasingMode", Easing.EaseOutQuad, this)
                    .setOnValueChange(mode -> moduleEntries.clear());
    private final BooleanValue noRenderModule = new BooleanValue("NoRenderModule", true);
    
    @Override
    public void onDisable() {
        super.onDisable();
        moduleEntries.clear();
    }
    
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (mc.gameSettings.showDebugInfo) return;
        
        FontRenderer font = FontManager.NotoSans.of(fontSize.getValue().intValue());
        
        if (moduleEntries.isEmpty()) { //lazy load
            for (Module module : Client.INSTANCE.getModuleManager()) {
                moduleEntries.add(new ModuleEntry(module, new Animation(moduleEasingMode.getValueByEasing(), 250)));
            }
        }
        
        List<ModuleEntry> sortedEntries = moduleEntries.stream()
                .filter(entry -> !(entry.module.getModuleCategory() == ModuleCategory.Render && noRenderModule.getValue()))
                .sorted(Comparator.comparingInt(entry -> -font.getStringWidth(
                        entry.module.getName() + (entry.module.getTag().isEmpty() ? "" : " §f" + entry.module.getTag())
                )))
                .collect(Collectors.toList());
        
        int y = 0;
        for (ModuleEntry entry : sortedEntries) {
            Module module = entry.module;
            Animation animation = entry.animation;
            
            animation.run(module.isToggled() ? 1.0 : 0.0);
            
            if (!animation.isFinished() || module.isToggled()) {
                String text = module.getName() + (module.getTag().isEmpty() ? "" : TextFormatting.GRAY + " " + module.getTag());
                int stringWidth = font.getStringWidth(text);
                int startX = event.getScaledResolution().getScaledWidth();
                
                font.drawString(text,
                        (float) (startX - stringWidth * animation.getValue() - 3.3f),
                        y + 1,
                        -1);
            }
            
            y += (int) ((font.getHeight() - 2) * animation.getValuef());
        }
    }
    
    
    private static class ModuleEntry {
        Module module;
        Animation animation;
        
        ModuleEntry(Module module, Animation animation) {
            this.module = module;
            this.animation = animation;
        }
    }
}
