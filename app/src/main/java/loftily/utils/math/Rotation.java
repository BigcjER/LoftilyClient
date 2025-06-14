package loftily.utils.math;

import loftily.handlers.impl.player.RotationHandler;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;

@AllArgsConstructor
public class Rotation {
    public float yaw, pitch;
    
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
        float f = sensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 1.2F;
        
        // get previous rotation
        Rotation rotation = RotationHandler.serverRotation;
        
        // fix yaw
        float deltaYaw = yaw - rotation.yaw;
        deltaYaw -= deltaYaw % gcd;
        yaw = rotation.yaw + deltaYaw;
        
        // fix pitch
        float deltaPitch = pitch - rotation.pitch;
        deltaPitch -= deltaPitch % gcd;
        pitch = rotation.pitch + deltaPitch;
        return this;
    }
}