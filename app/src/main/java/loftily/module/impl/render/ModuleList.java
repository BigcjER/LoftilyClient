package loftily.module.impl.render;

import loftily.Client;
import loftily.config.impl.json.DragsJsonConfig;
import loftily.event.impl.render.Render2DEvent;
import loftily.event.impl.world.WorldLoadEvent;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.gui.interaction.draggable.Draggable;
import loftily.gui.interaction.draggable.IDraggable;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.EasingModeValue;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@ModuleInfo(name = "ModuleList", category = ModuleCategory.RENDER, defaultToggled = true)
public class ModuleList extends Module implements IDraggable {
    //不是value
    private final List<ModuleEntry> moduleEntries = new ArrayList<>();
    private final Animation startXAnimation = new Animation(Easing.EaseOutExpo, 500);
    //Text
    private final NumberValue fontSize = new NumberValue("FontSize", 18, 10, 30);
    private final BooleanValue noRenderModule = new BooleanValue("NoRenderModule", true);
    private final BooleanValue fontShadow = new BooleanValue("FontShadow", true);
    //TextColor
    private final ModeValue textColorMode = new ModeValue("TextColorMode", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Rainbow"),
            new StringMode("AnotherRainbow"),
            new StringMode("Fade"));
    private final NumberValue textColorRed = new NumberValue("TextColorRed", 255, 0, 255)
            .setVisible(() -> textColorMode.is("Normal") || textColorMode.is("Fade"));
    private final NumberValue textColorGreen = new NumberValue("TextColorGreen", 255, 0, 255)
            .setVisible(() -> textColorMode.is("Normal") || textColorMode.is("Fade"));
    private final NumberValue textColorBlue = new NumberValue("TextColorBlue", 255, 0, 255)
            .setVisible(() -> textColorMode.is("Normal") || textColorMode.is("Fade"));
    //Tag
    private final BooleanValue drawTag = new BooleanValue("DrawTag", true);
    private final ModeValue tagColorMode = new ModeValue("TagColorMode", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Rainbow"),
            new StringMode("AnotherRainbow"),
            new StringMode("Fade"))
            .setVisible(drawTag::getValue);
    private final NumberValue tagColorRed = new NumberValue("TagColorRed", 130, 0, 255)
            .setVisible(() -> tagColorMode.is("Normal") || tagColorMode.is("Fade") && drawTag.getValue());
    private final NumberValue tagColorGreen = new NumberValue("TagColorGreen", 130, 0, 255)
            .setVisible(() -> tagColorMode.is("Normal") || tagColorMode.is("Fade") && drawTag.getValue());
    private final NumberValue tagColorBlue = new NumberValue("TagColorBlue", 130, 0, 255)
            .setVisible(() -> tagColorMode.is("Normal") || tagColorMode.is("Fade") && drawTag.getValue());
    //BackGround
    private final BooleanValue drawTextBackGround = new BooleanValue("DrawTextBackGround", true);
    private final ModeValue backGroundColorMode = new ModeValue("BackGroundColor", "Normal", this,
            new StringMode("Normal"),
            new StringMode("Rainbow"),
            new StringMode("AnotherRainbow"),
            new StringMode("Fade"))
            .setVisible(drawTextBackGround::getValue);
    private final NumberValue backGroundColorRed = new NumberValue("BackGroundColorRed", 0, 0, 255)
            .setVisible(() -> backGroundColorMode.is("Normal") || backGroundColorMode.is("Fade") && drawTextBackGround.getValue());
    private final NumberValue backGroundColorGreen = new NumberValue("BackGroundColorGreen", 0, 0, 255)
            .setVisible(() -> backGroundColorMode.is("Normal") || backGroundColorMode.is("Fade") && drawTextBackGround.getValue());
    private final NumberValue backGroundColorBlue = new NumberValue("BackGroundColorBlue", 0, 0, 255)
            .setVisible(() -> backGroundColorMode.is("Normal") || backGroundColorMode.is("Fade") && drawTextBackGround.getValue());
    private final NumberValue backGroundColorAlpha = new NumberValue("BackGroundColorAlpha", 60, 0, 255)
            .setVisible(drawTextBackGround::getValue);
    private final EasingModeValue moduleEasingMode = (EasingModeValue)
            new EasingModeValue("ModuleEasingMode", Easing.EaseOutQuad, this)
                    .setOnValueChange(mode -> moduleEntries.clear());
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
            draggable = new Draggable(event.getScaledResolution().getScaledWidth() - 3, 0, event.getScaledResolution(), 1);
            Client.INSTANCE.getFileManager().get(DragsJsonConfig.class).read();
        }
        
        if (moduleEntries.isEmpty()) {
            for (Module module : Client.INSTANCE.getModuleManager().getAll()) {
                moduleEntries.add(new ModuleEntry(module, new Animation(moduleEasingMode.getValueByEasing(), 250)));
            }
        }
        
        Point mouse = RenderUtils.getMouse(event.getScaledResolution());
        
        FontRenderer font = FontManager.NotoSans.of(fontSize.getValue().intValue());
        
        List<ModuleEntry> sortedEntries = moduleEntries.stream()
                .filter(entry -> !(entry.module.getModuleCategory() == ModuleCategory.RENDER && noRenderModule.getValue()))
                .sorted(Comparator.comparingInt(entry -> -font.getStringWidth(getText(entry.module))))
                .collect(Collectors.toList());
        
        
        //计算总高度，最长的模块
        int longestStringWidth = 0;
        int totalHeight = 0;
        for (ModuleEntry entry : sortedEntries) {
            entry.animation.run(entry.module.isToggled() ? 1.0 : 0.0);
            totalHeight += (int) ((font.getHeight() - 2) * entry.animation.getValuef());
            
            Module module = entry.module;
            String text = getText(module);
            int stringWidth = font.getStringWidth(text);
            if (stringWidth > longestStringWidth) {
                longestStringWidth = stringWidth;
            }
        }
        
        //更新位置
        getDraggable().updateDrag(
                mouse.x,
                mouse.y,
                longestStringWidth,
                totalHeight,
                event.getScaledResolution().getScaledWidth(),
                event.getScaledResolution().getScaledHeight());
        
        int finalLongestStringWidth = longestStringWidth;
        
        int width = event.getScaledResolution().getScaledWidth();
        int height = event.getScaledResolution().getScaledHeight();
        
        AtomicInteger indexAll = new AtomicInteger();
        AtomicInteger indexTag = new AtomicInteger();
        getDraggable().applyDragEffect(() -> {
            startXAnimation.run(1);
            int x = getDraggable().getPosX(width);
            int y = getDraggable().getPosY(height);
            
            for (ModuleEntry entry : sortedEntries) {
                Module module = entry.module;
                Animation animation = entry.animation;
                
                if (!module.isToggled() && animation.isFinished() || animation.getValuef() <= 0) continue;
                String name = module.getName();
                String tag = module.getTag();
                
                int nameWidth = font.getStringWidth(name);
                int tagWidth = font.getStringWidth(tag);
                int spaceWidth = font.getStringWidth(" ");
                int fullWidth = drawTag.getValue() ? nameWidth + (tag.isEmpty() ? 0 : spaceWidth) + tagWidth : nameWidth;
                
                //计算位置
                boolean isLeft = event.getScaledResolution().getScaledWidth() / 2 > (x + finalLongestStringWidth / 2);
                float baseX = getDraggable().getPosX(width);
                float drawX;
                
                if (isLeft) {
                    drawX = baseX * startXAnimation.getValuef();
                    drawX -= fullWidth * (1.0f - animation.getValuef());
                } else {
                    drawX = baseX * startXAnimation.getValuef() + finalLongestStringWidth - fullWidth * animation.getValuef();
                }
                
                //颜色，绘制
                Color textColor = getColor(textColorMode.getValueByName(), textColorRed.getValue().intValue(), textColorGreen.getValue().intValue(), textColorBlue.getValue().intValue(), indexAll.get());
                Color tagColor = getColor(tagColorMode.getValueByName(), tagColorRed.getValue().intValue(), tagColorGreen.getValue().intValue(), tagColorBlue.getValue().intValue(), indexTag.get());
                Color backGroundColor = getColor(backGroundColorMode.getValueByName(), backGroundColorRed.getValue().intValue(), backGroundColorGreen.getValue().intValue(), backGroundColorBlue.getValue().intValue(), indexAll.get());
                backGroundColor = ColorUtils.colorWithAlpha(backGroundColor, backGroundColorAlpha.getValue().intValue());
                
                if (drawTextBackGround.getValue())
                    RenderUtils.drawRectHW(drawX - 1.5F, y - 1, fullWidth + 4, font.getHeight() - 2, backGroundColor);
                
                if (!fontShadow.getValue()) {
                    font.drawString(name, drawX, y, textColor);
                    if (drawTag.getValue())
                        font.drawString(tag, drawX + nameWidth + spaceWidth, y, tagColor);
                } else {
                    font.drawStringWithShadow(name, drawX, y, textColor.getRGB());
                    if (drawTag.getValue())
                        font.drawStringWithShadow(tag, drawX + nameWidth + spaceWidth, y, tagColor.getRGB());
                }
                
                y += (int) ((font.getHeight() - 2) * animation.getValuef());
                indexAll.getAndIncrement();
                if (!module.getTag().isEmpty()) indexTag.getAndIncrement();
            }
        }, event.getScaledResolution());
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        startXAnimation.run(0);
        startXAnimation.setValue(0);
    }
    
    private Color getColor(String mode, int red, int green, int blue, int index) {
        Color color = Color.WHITE;
        switch (mode.toLowerCase()) {
            case "normal":
                color = new Color(red, green, blue);
                break;
            case "rainbow":
                color = ColorUtils.rainbow(index + index * 200000000L);
                break;
            case "fade":
                color = ColorUtils.fade(new Color(red, green, blue), index, 10);
                break;
            case "anotherrainbow":
                color = ColorUtils.rainbow2(index);
                break;
        }
        
        return color;
    }
    
    private String getText(Module module) {
        return module.getName() + (drawTag.getValue() ? (module.getTag().isEmpty() ? "" : TextFormatting.GRAY + " " + module.getTag()) : "");
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
