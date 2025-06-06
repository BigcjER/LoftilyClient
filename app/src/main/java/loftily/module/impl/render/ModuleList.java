package loftily.module.impl.render;

import loftily.Client;
import loftily.config.impl.DragsConfig;
import loftily.event.impl.render.Render2DEvent;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.gui.interaction.draggable.Draggable;
import loftily.gui.interaction.draggable.IDraggable;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.math.Pair;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.EasingModeValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ModuleInfo(name = "ModuleList", category = ModuleCategory.RENDER, defaultToggled = true)
public class ModuleList extends Module implements IDraggable {
    
    private final List<ModuleEntry> moduleEntries = new ArrayList<>();
    
    private final NumberValue fontSize = new NumberValue("FontSize", 18, 10, 30);
    private final EasingModeValue moduleEasingMode = (EasingModeValue)
            new EasingModeValue("ModuleEasingMode", Easing.EaseOutQuad, this)
                    .setOnValueChange(mode -> moduleEntries.clear());
    private final BooleanValue noRenderModule = new BooleanValue("NoRenderModule", true);
    private final BooleanValue fontShadow = new BooleanValue("FontShadow", true);
    private Draggable draggable;
    
    
    @Override
    public void onDisable() {
        super.onDisable();
        moduleEntries.clear();
    }
    
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (mc.gameSettings.showDebugInfo) return;
        
        if (draggable == null) {
            draggable = new Draggable(event.getScaledResolution().getScaledWidth() - 3, 0, 1);
            Client.INSTANCE.getFileManager().get(DragsConfig.class).read();
        }
        
        if (moduleEntries.isEmpty()) {
            for (Module module : Client.INSTANCE.getModuleManager()) {
                moduleEntries.add(new ModuleEntry(module, new Animation(moduleEasingMode.getValueByEasing(), 250)));
            }
        }
        
        Pair<Integer, Integer> pair = RenderUtils.getMouse(event.getScaledResolution());
        
        FontRenderer font = FontManager.NotoSans.of(fontSize.getValue().intValue());
        
        List<ModuleEntry> sortedEntries = moduleEntries.stream()
                .filter(entry -> !(entry.module.getModuleCategory() == ModuleCategory.RENDER && noRenderModule.getValue()))
                .sorted(Comparator.comparingInt(entry -> -font.getStringWidth(
                        entry.module.getName() + (entry.module.getTag().isEmpty() ? "" : " §f" + entry.module.getTag())
                )))
                .collect(Collectors.toList());
        
        
        //计算总高度，最长的模块
        int longestStringWidth = 0;
        int totalHeight = 0;
        for (ModuleEntry entry : sortedEntries) {
            entry.animation.run(entry.module.isToggled() ? 1.0 : 0.0);
            totalHeight += (int) ((font.getHeight() - 2) * entry.animation.getValuef());
            
            Module module = entry.module;
            String text = module.getName() + (module.getTag().isEmpty() ? "" : TextFormatting.GRAY + " " + module.getTag());
            int stringWidth = font.getStringWidth(text);
            if (stringWidth > longestStringWidth) {
                longestStringWidth = stringWidth;
            }
        }
        
        getDraggable().updateDrag(
                pair.getFirst(),
                pair.getSecond(),
                longestStringWidth,
                totalHeight,
                event.getScaledResolution().getScaledWidth(),
                event.getScaledResolution().getScaledHeight());
        
        int finalLongestStringWidth = longestStringWidth;
        getDraggable().applyDragEffect(() -> {
            int y = getDraggable().getPosY();
            int x = getDraggable().getPosX();
            for (ModuleEntry entry : sortedEntries) {
                Module module = entry.module;
                Animation animation = entry.animation;
                
                if (!module.isToggled() && animation.isFinished()) continue;
                String text = module.getName() + (module.getTag().isEmpty() ? "" : TextFormatting.GRAY + " " + module.getTag());
                int stringWidth = font.getStringWidth(text);
                
                boolean isLeft = event.getScaledResolution().getScaledWidth() / 2 > (x + finalLongestStringWidth / 2);
                float baseX = getDraggable().getPosX();
                float drawX;
                
                if (isLeft) {
                    drawX = baseX;
                    drawX -= stringWidth * (1.0f - animation.getValuef());
                } else {
                    drawX = baseX + finalLongestStringWidth - stringWidth * animation.getValuef();
                }
                
                
                if (!fontShadow.getValue())
                    font.drawString(text, drawX, y, -1);
                else
                    font.drawStringWithShadow(text, drawX, y, -1);
                y += (int) ((font.getHeight() - 2) * animation.getValuef());
            }
        });
    }
    
    @Override
    public Draggable getDraggable() {
        return draggable;
    }
    
    @Override
    public String getName() {
        return "ModuleList";
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
