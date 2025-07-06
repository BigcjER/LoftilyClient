package loftily.utils.render;

import loftily.utils.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

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
    
    public static void drawArc(float centerX, float centerY, float radius,
                               float startAngleDegrees, float endAngleDegrees,
                               float thickness, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(thickness);
        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        
        
        final float angleStepDegrees = 3.6f;
        
        float angleSweep = Math.abs(endAngleDegrees - startAngleDegrees);
        int segments = (int) Math.ceil(angleSweep / angleStepDegrees);
        //System.out.println(segments);
        for (int i = 0; i <= segments; i++) {
            float currentAngleDegrees = startAngleDegrees + i * angleStepDegrees;
            if (currentAngleDegrees > endAngleDegrees) {
                currentAngleDegrees = endAngleDegrees;
            }
            
            double currentAngleRad = Math.toRadians(currentAngleDegrees);
            float x = centerX + (float) (radius * Math.cos(currentAngleRad));
            float y = centerY + (float) (radius * Math.sin(currentAngleRad));
            GL11.glVertex2f(x, y);
            
            if (currentAngleDegrees == endAngleDegrees) break;
        }
        
        double endAngleRad = Math.toRadians(endAngleDegrees);
        float endX = centerX + (float) (radius * Math.cos(endAngleRad));
        float endY = centerY + (float) (radius * Math.sin(endAngleRad));
        GL11.glVertex2f(endX, endY);
        GL11.glEnd();
        
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        resetColor();
    }
    
    public static void drawRectHW(int x, int y, int width, int height, Color color) {
        drawRect(x, y, x + width, y + height, color.getRGB());
    }
    
    public static void drawRectHW(float x, float y, float width, float height, Color color) {
        drawRect(x, y, x + width, y + height, color.getRGB());
    }
    
    public static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }
        
        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }
        
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, alpha);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    public static void startGlStencil(Runnable stencil, boolean equal) {
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
        GL11.glDepthFunc(equal ? GL11.GL_EQUAL : GL11.GL_NOTEQUAL);
    }
    
    
    public static void startGlStencil(Runnable stencil) {
        startGlStencil(stencil, true);
    }
    
    public static void stopGlStencil() {
        GL11.glDepthMask(true);
        GL11.glClearDepth(1.0);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }
    
    public static void startGlScissor(int x, int y, int width, int height) {
        GL11.glEnable(GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(mc);
        double scale = sr.getScaleFactor();
        
        y = sr.getScaledHeight() - y;
        
        x *= (int) scale;
        y *= (int) scale;
        width *= (int) scale;
        height *= (int) scale;
        
        GL11.glScissor(x, y - height, width, height);
    }
    
    public static void stopGlScissor() {
        GL11.glDisable(GL_SCISSOR_TEST);
    }
    
    public static boolean isHovering(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
    
    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public static void drawImage(ResourceLocation imageLocation, float x, float y, float width, float height) {
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        mc.getTextureManager().bindTexture(imageLocation);
        Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, 0, 0, (int) width, (int) height, width, height);
        GlStateManager.resetColor();
        GlStateManager.disableBlend();
    }
    
    public static Point getMouse(ScaledResolution scaledResolution) {
        if (mc.currentScreen == null) return new Point(-1, -1);
        
        int screenWidth = scaledResolution.getScaledWidth();
        int j1 = scaledResolution.getScaledHeight();
        final int mouseX = Mouse.getX() * screenWidth / mc.displayWidth;
        final int mouseY = j1 - Mouse.getY() * j1 / mc.displayHeight - 1;
        
        return new Point(mouseX, mouseY);
    }
    
    public static void drawPlayerHead(int x, int y, int headSize, Entity entity) {
        NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(entity.getUniqueID());
        if (playerInfo != null) {
            mc.getTextureManager().bindTexture(playerInfo.getLocationSkin());
            GL11.glColor4f(1F, 1F, 1F, 1F);
            Gui.drawScaledCustomSizeModalRect(x + 5, y + 5, 8F, 8F, 8, 8, headSize, headSize, 64F, 64F);
            GlStateManager.resetColor();
        } else {
            RenderUtils.drawRoundedRect(x + 5, y + 5, headSize, headSize, 3, Colors.OnBackGround.color);
        }
    }
    
}
