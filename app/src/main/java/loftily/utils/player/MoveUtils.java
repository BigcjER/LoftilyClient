package loftily.utils.player;

import loftily.utils.client.ClientUtils;

public class MoveUtils implements ClientUtils {
    
    public static double getSpeed(double motionX, double motionZ) {
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }
    
    public static double getSpeed() {
        return getSpeed(mc.player.motionX, mc.player.motionZ);
    }
    
    public static double getDirection(float rotationYaw, double moveForward, double moveStrafe) {
        if (moveForward == 0.0F && moveStrafe == 0.0F) {
            return Math.toRadians(rotationYaw);
        }
        
        double angle = Math.atan2(-moveStrafe, moveForward);
        
        return angle + Math.toRadians(rotationYaw);
    }
    
    public static double getDirection() {
        return getDirection(mc.player.rotationYaw, mc.player.movementInput.moveForward, mc.player.movementInput.moveStrafe);
    }
    
    public static boolean isMoving() {
        return mc.player.movementInput.moveForward != 0f || mc.player.movementInput.moveStrafe != 0f;
    }
    
    public static void setSpeed(final double speed, boolean movingCheck) {
        if (!isMoving() && movingCheck)
            return;
        
        final double yaw = getDirection();
        mc.player.motionX = -Math.sin(yaw) * speed;
        mc.player.motionZ = Math.cos(yaw) * speed;
    }
    
    public static void strafe() {
        setSpeed(getSpeed(), true);
    }
    
    public static void stop() {
        mc.player.motionX = 0;
        mc.player.motionZ = 0;
    }
}
