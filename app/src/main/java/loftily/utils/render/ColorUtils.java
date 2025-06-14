package loftily.utils.render;

import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class ColorUtils {
    public static Color colorWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }
    
    public static Color rainbow(long offset) {
        float hue = (float) (System.nanoTime() + offset) / 6.0E9F % 1.0F;
        long c = Long.parseLong(Integer.toHexString(Color.HSBtoRGB(hue, 1.0F, 1F)), 16);
        Color color = new Color((int) c);
        return new Color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }
    
    public static Color rainbow2(int index) {
        double currentColor = Math.ceil((System.currentTimeMillis() + (index * 70L)) / 7.0);
        
        currentColor %= 360.0;
        
        float hue = (float) (currentColor / 360.0);
        
        return Color.getHSBColor(hue, 0.5F, 1F);
    }
    
    public static Color fade(Color color, int index, int count) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float brightness = Math.abs(((System.currentTimeMillis() % 2000L) / 1000.0f
                + (float) index / count * 2.0f) % 2.0f - 1.0f);
        brightness = 0.5f + 0.5f * brightness;
        hsb[2] = brightness % 2.0f;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }
}
