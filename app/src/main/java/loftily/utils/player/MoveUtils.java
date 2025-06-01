package loftily.utils.player;

import loftily.utils.client.ClientUtils;

public class MoveUtils implements ClientUtils {
    public static double getDirection(float rotationYaw, double moveForward, double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;
        
        float forward = 1F;
        
        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;
        
        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;
        
        return Math.toRadians(rotationYaw);
    }
    
    public static double getDirection() {
        return getDirection(mc.player.movementYaw, mc.player.movementInput.moveForward, mc.player.moveStrafing);
    }
    
    public static boolean isMoving() {
        return mc.player.movementInput.moveForward != 0f || mc.player.movementInput.moveStrafe != 0f;
    }
    
    public static void setSpeed(final double speed,boolean movingCheck) {
        if (!isMoving() && movingCheck)
            return;
        
        final double yaw = getDirection();
        mc.player.motionX = -Math.sin(yaw) * speed;
        mc.player.motionZ = Math.cos(yaw) * speed;
    }
    
    public static void stop() {
        mc.player.motionX = 0;
        mc.player.motionZ = 0;
    }
}
