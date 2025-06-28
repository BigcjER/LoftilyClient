package loftily.utils.player;

import loftily.utils.client.ClientUtils;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

public class MoveUtils implements ClientUtils {
    
    public static double getSpeed(double motionX, double motionZ) {
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }
    
    public static double getSpeed() {
        return getSpeed(mc.player.motionX, mc.player.motionZ);
    }

    public static double getHorizontalSpeed() {
        return getHorizontalSpeed(mc.player);
    }

    public static double getHorizontalSpeed(Entity entity) {
        return Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
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

    public static int getSpeedAmplifier() {
        return mc.player.isPotionActive(MobEffects.SPEED) ? 1 + mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() : 0;
    }
    
    public static void strafe() {
        setSpeed(getSpeed(), true);
    }
    
    public static void stop(boolean y) {
        mc.player.motionX = 0;
        mc.player.motionZ = 0;
        if(y){
            mc.player.motionY = 0;
        }
    }

    public static double getMovementAngle() {
        double angle = Math.toDegrees(Math.atan2(-mc.player.moveStrafing, mc.player.moveForward));
        return angle == 0.0 ? 0.0 : angle;
    }

    public static float getMotionYaw() {
        return (float)Math.toDegrees(Math.atan2(mc.player.motionZ, mc.player.motionX)) - 90.0F;
    }
}