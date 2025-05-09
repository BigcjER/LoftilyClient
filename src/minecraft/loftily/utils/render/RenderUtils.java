package loftily.utils.render;

import loftily.utils.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtils implements ClientUtils {
    private final static ShaderUtils roundedShader = new ShaderUtils(Shader.RoundedRect);
    private final static ShaderUtils roundedOutlineShader = new ShaderUtils(Shader.RoundRectOutline);
    
    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        RenderUtils.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        roundedShader.start();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        roundedShader.setUniformf("location", x * sr.getScaleFactor(), Minecraft.getMinecraft().displayHeight - height * sr.getScaleFactor() - y * sr.getScaleFactor());
        roundedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedShader.setUniformf("radius", radius * sr.getScaleFactor());
        roundedShader.setUniformf("color", color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        ShaderUtils.drawQuad(x - 1.0f, y - 1.0f, width + 2.0f, height + 2.0f);
        roundedShader.stop();
        GlStateManager.disableBlend();
    }
    
    public static void drawRoundedRectOutline(float x, float y, float width, float height, float radius, float outlineThickness, Color color, Color outlineColor) {
        RenderUtils.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        roundedOutlineShader.start();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        roundedOutlineShader.setUniformf("location", x * sr.getScaleFactor(), Minecraft.getMinecraft().displayHeight - height * sr.getScaleFactor() - y * sr.getScaleFactor());
        roundedOutlineShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedOutlineShader.setUniformf("radius", radius * sr.getScaleFactor());
        roundedOutlineShader.setUniformf("outlineThickness", outlineThickness * sr.getScaleFactor());
        roundedOutlineShader.setUniformf("color", color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        roundedOutlineShader.setUniformf("outlineColor", outlineColor.getRed() / 255.0f, outlineColor.getGreen() / 255.0f, outlineColor.getBlue() / 255.0f, outlineColor.getAlpha() / 255.0f);
        ShaderUtils.drawQuad(x - (2.0f + outlineThickness), y - (2.0f + outlineThickness), width + (4.0f + outlineThickness * 2.0f), height + (4.0f + outlineThickness * 2.0f));
        roundedOutlineShader.stop();
        GlStateManager.disableBlend();
    }
    
    
    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public static void drawRectHW(int x, int y, int width, int height, Color color) {
        Gui.drawRect(x, y, x + width, y + height, color.getRGB());
    }
    
    public static void startGlStencil(Runnable stencil) {
        GL11.glDepthMask(true);
        GL11.glClearDepth(1.0);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glColorMask(false, false, false, false);
        stencil.run();
        GL11.glDepthMask(false);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthFunc(GL11.GL_EQUAL);
    }
    
    public static void stopGlStencil() {
        GL11.glDepthMask(true);
        GL11.glClearDepth(1.0);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }
    
    public static boolean isHovering(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
