package loftily.utils.render;

import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class ColorUtils {
    public static Color colorWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }
}
