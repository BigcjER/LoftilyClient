package loftily.utils.math;

import loftily.handlers.impl.player.RotationHandler;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;

import static java.lang.Math.round;

@AllArgsConstructor
public class Rotation {
    public float yaw, pitch;
    
    public static float getFixedAngleDelta(float sensitivity) {
        return (sensitivity * 0.6f + 0.2f) * (sensitivity * 0.6f + 0.2f) * (sensitivity * 0.6f + 0.2f) * 1.2f;
    }
    
    public static float getFixedSensitivityAngle(float targetAngle, float startAngle, float gcd) {
        return startAngle + (round((targetAngle - startAngle) / gcd) * gcd);
    }
    
    @Override
    public String toString() {
        return String.format("Yaw:%.2f, Pitch:%.2f", yaw, pitch);
    }
    
    public Rotation add(float yaw, float pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
        return this;
    }
    
    public Rotation fixedSensitivity(float sensitivity) {
        if (sensitivity == 0) {
            sensitivity = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
        }
        float gcd = getFixedAngleDelta(sensitivity);
        
        yaw = getFixedSensitivityAngle(yaw, RotationHandler.getRotation().yaw, gcd);
        pitch = Math.max(-90f, Math.min(90f, getFixedSensitivityAngle(pitch, RotationHandler.getRotation().pitch, gcd)));
        
        return this;
    }
}