package loftily.module.impl.render;

import loftily.Client;
import loftily.config.impl.json.DragsJsonConfig;
import loftily.event.impl.render.Render2DEvent;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.gui.interaction.draggable.Draggable;
import loftily.gui.interaction.draggable.IDraggable;
import loftily.module.Module;
import loftily.module.ModuleCategory;
import loftily.module.ModuleInfo;
import loftily.utils.other.StringUtils;
import loftily.utils.player.MoveUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.MultiBooleanValue;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ModuleInfo(name = "InfoHUD", category = ModuleCategory.RENDER)
public class InfoHUD extends Module implements IDraggable {
    private final MultiBooleanValue displayItemsValue = new MultiBooleanValue("DisplayItems")
            .add("SessionTime", false)
            .add("SessionName", true)
            .add("FPS", true)
            .add("BPS", true)
            .add("Kills", false);
    
    private final Map<String, String> displayItems = new LinkedHashMap<>();
    private final int TEXT_PADDING = 8, PADDING = 2, RADIUS = 2;
    private Draggable draggable;
    
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        final int screenWidth = event.getScaledResolution().getScaledWidth();
        final int screenHeight = event.getScaledResolution().getScaledHeight();
        
        if (draggable == null) {
            draggable = new Draggable(0, screenHeight / 2 - 100, event.getScaledResolution(), 0);
            Client.INSTANCE.getFileManager().get(DragsJsonConfig.class).read();
        }
        
        final int TITLE_FONT_SIZE = 15;
        final FontRenderer TITLE_FONT = FontManager.NotoSans.of(TITLE_FONT_SIZE);
        
        updateDisplayItems();
        
        final int TITLE_HEIGHT = PADDING * 5;
        final int HEAD_SIZE = 40;
        
        int width = 0;
        for (Map.Entry<String, String> entry : displayItems.entrySet()) {
            String text = entry.getKey() + ": " + entry.getValue();
            
            int thisWidth = TITLE_FONT.getStringWidth(text) + HEAD_SIZE;
            width = Math.max(width, thisWidth);
        }
        
        int backGroundWidth = Math.max(width, 140);
        int backGroundHeight = HEAD_SIZE + TITLE_HEIGHT + PADDING * 3;
        
        AtomicInteger yOffset = new AtomicInteger();
        draggable.applyDragEffect(() -> {
            int startX = draggable.getPosX(screenWidth);
            int startY = draggable.getPosY(screenHeight);
            
            //BackGround
            RenderUtils.drawRoundedRect(startX, startY, backGroundWidth, backGroundHeight, RADIUS, Colors.BackGround.color);
            RenderUtils.drawRoundedRect(startX, startY, backGroundWidth, TITLE_HEIGHT, RADIUS, Colors.OnBackGround.color);
            
            //TitleText
            TITLE_FONT.drawString("Info HUD", startX + PADDING, startY + 2, Colors.Text.color);
            //Head
            RenderUtils.drawPlayerHead(startX - 2, (startY + TITLE_HEIGHT) - PADDING / 2 - 1, HEAD_SIZE, mc.player);
            RenderUtils.drawRoundedRectOutline(
                    startX + PADDING,
                    (startY + TITLE_HEIGHT) + PADDING,
                    HEAD_SIZE + PADDING, HEAD_SIZE + PADDING,
                    RADIUS, 1,
                    new Color(0, 0, 0, 0), Colors.BackGround.color);
            
            for (Map.Entry<String, String> entry : displayItems.entrySet()) {
                int textStartX = startX + HEAD_SIZE + PADDING * 4;
                
                FontRenderer textFont = FontManager.NotoSans.of(TITLE_FONT_SIZE - 2);
                textFont.drawString(
                        entry.getKey(),
                        textStartX,
                        startY + yOffset.get() + PADDING * 2 + TITLE_HEIGHT,
                        Colors.Text.color.darker());
                
                textFont.drawString(
                        entry.getValue(),
                        textStartX + textFont.getStringWidth(entry.getKey() + " "),
                        startY + yOffset.get() + PADDING * 2 + TITLE_HEIGHT,
                        Colors.Text.color);
                
                yOffset.addAndGet(TEXT_PADDING);
            }
        }, event.getScaledResolution());
        
        Point mouse = RenderUtils.getMouse(event.getScaledResolution());
        draggable.updateDrag(mouse.x, mouse.y, backGroundWidth, backGroundHeight, screenWidth, screenHeight);
    }
    
    private void updateDisplayItems() {
        displayItems.clear();
        
        for (Map.Entry<String, Boolean> entry : displayItemsValue.getValue().entrySet()) {
            if (entry.getValue()) {
                switch (entry.getKey()) {
                    case "SessionTime":
                        displayItems.put("Session time", StringUtils.convertMillis(System.currentTimeMillis() - mc.getSession().getSessionStartTime()));
                        break;
                    
                    case "SessionName":
                        displayItems.put("Name", mc.getSession().getUsername());
                        break;
                    
                    case "Kills":
                        displayItems.put("Kills", String.valueOf(mc.player.kills));
                        break;
                    
                    case "FPS":
                        displayItems.put("FPS", String.valueOf(Minecraft.getDebugFPS()));
                        break;
                    
                    case "BPS":
                        displayItems.put("BPS", String.valueOf(MoveUtils.getBPS()));
                        break;
                }
            }
        }
    }
    
    @Override
    public Draggable getDraggable() {
        return draggable;
    }
}
