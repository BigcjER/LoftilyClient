package loftily.gui.menu;

import loftily.gui.font.FontManager;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public enum SplashScreen {
    INSTANCE;
    
    private float targetProgress;
    private float currentProgress;
    private String text;
    
    private long lastUpdateTime;
    private boolean isAnimating;
    
    
    public void setProgressAndDraw(String text, int targetProgress) {
        this.text = text;
        this.targetProgress = MathHelper.clamp(targetProgress, 0, 100);
        if (!isAnimating) {
            this.currentProgress = Math.min(this.currentProgress, this.targetProgress);
            this.lastUpdateTime = System.currentTimeMillis();
            
            if (isAnimating) return;
            isAnimating = true;
            Minecraft.getMinecraft().addScheduledTask(this::updateAnimation);
        }
    }
    
    private void updateAnimation() {
        if (!isAnimating) return;
        
        float delta = (System.currentTimeMillis() - lastUpdateTime);
        lastUpdateTime = System.currentTimeMillis();
        
        currentProgress = Math.min(currentProgress + (delta * 0.15f), targetProgress);
        
        drawSplashScreen(Minecraft.getMinecraft());
        
        if (currentProgress < targetProgress) {
            Minecraft.getMinecraft().addScheduledTask(this::updateAnimation);
        } else {
            isAnimating = false;
        }
    }
    
    private void drawSplashScreen(Minecraft mc) {
        ScaledResolution sr = new ScaledResolution(mc);
        
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, sr.getScaledWidth(), sr.getScaledHeight(), 0.0, 1000.0, 3000.0);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0f, 0.0f, -2000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        
        draw(sr, 255);
        
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.popMatrix();
        
        mc.updateDisplay();
    }
    
    public void draw(ScaledResolution sr, int alpha) {
        
        RenderUtils.drawRectHW(0, 0, sr.getScaledWidth(), sr.getScaledHeight(),
                Colors.BackGround.color);
        
        FontManager.NotoSans.of(30).drawCenteredString(
                "Initialization " + text,
                sr.getScaledWidth() / 2F,
                sr.getScaledHeight() / 2F - 20,
                Colors.Text.color);
        
        RenderUtils.drawRectHW(0, sr.getScaledHeight() - 2, sr.getScaledWidth() * (currentProgress / 100F), 2,
                Colors.Active.color);
        
        RenderUtils.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), (Math.min(255, 255 - alpha) << 24));
    }
}