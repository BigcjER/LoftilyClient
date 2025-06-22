package loftily.utils.player;

import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

import static loftily.utils.client.ClientUtils.mc;

public class ScaffoldUtils {
    public static boolean scaffoldDiagonal(boolean strict) {
        float back = MathHelper.wrapAngleTo180_float(mc.player.rotationYaw) - hardcodedYaw();
        float yaw = (back % 360.0F + 360.0F) % 360.0F;
        yaw = yaw > 180.0F ? yaw - 360.0F : yaw;
        boolean isYawDiagonal = inBetween(-170.0, 170.0, yaw) && !inBetween(-10.0, 10.0, yaw) && !inBetween(80.0, 100.0, yaw) && !inBetween(-100.0, -80.0, yaw);
        if (strict) {
            isYawDiagonal = inBetween(-178.5, 178.5, yaw) && !inBetween(-1.5, 1.5, yaw) && !inBetween(88.5, 91.5, yaw) && !inBetween(-91.5, -88.5, yaw);
        }

        return isYawDiagonal;
    }

    public static boolean inBetween(double min, double max, double value) {
        return value >= min && value <= max;
    }

    public static float hardcodedYaw() {
        float simpleYaw = 0.0F;
        boolean w = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        boolean s = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        boolean a = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        boolean d = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        boolean dupe = a & d;
        if (w) {
            simpleYaw -= 180.0F;
            if (!dupe) {
                if (a) {
                    simpleYaw += 45.0F;
                }

                if (d) {
                    simpleYaw -= 45.0F;
                }
            }
        } else if (!s) {
            simpleYaw -= 180.0F;
            if (!dupe) {
                if (a) {
                    simpleYaw += 90.0F;
                }

                if (d) {
                    simpleYaw -= 90.0F;
                }
            }
        } else if (!w && !dupe) {
            if (a) {
                simpleYaw -= 45.0F;
            }

            if (d) {
                simpleYaw += 45.0F;
            }
        }

        return simpleYaw;
    }
}
